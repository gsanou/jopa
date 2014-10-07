package cz.cvut.kbss.jopa.oom;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cvut.kbss.jopa.exceptions.StorageAccessException;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.Metamodel;
import cz.cvut.kbss.jopa.oom.exceptions.EntityDeconstructionException;
import cz.cvut.kbss.jopa.oom.exceptions.EntityReconstructionException;
import cz.cvut.kbss.jopa.oom.exceptions.UnpersistedChangeException;
import cz.cvut.kbss.jopa.sessions.UnitOfWorkImpl;
import cz.cvut.kbss.jopa.utils.EntityPropertiesUtils;
import cz.cvut.kbss.ontodriver.exceptions.OntoDriverException;
import cz.cvut.kbss.ontodriver_new.AxiomDescriptor;
import cz.cvut.kbss.ontodriver_new.Connection;
import cz.cvut.kbss.ontodriver_new.MutationAxiomDescriptor;
import cz.cvut.kbss.ontodriver_new.model.Assertion;
import cz.cvut.kbss.ontodriver_new.model.Axiom;
import cz.cvut.kbss.ontodriver_new.model.NamedResource;

public class ObjectOntologyMapperImpl implements ObjectOntologyMapper, EntityMappingHelper {

	private static final Logger LOG = Logger.getLogger(ObjectOntologyMapperImpl.class.getName());

	private final UnitOfWorkImpl uow;
	private final Connection storageConnection;
	private final Metamodel metamodel;

	private final AxiomDescriptorFactory descriptorFactory;
	private final EntityConstructor entityBuilder;
	private final EntityDeconstructor entityBreaker;
	private final InstanceRegistry instanceRegistry;
	private final PendingChangeRegistry pendingPersists;

	public ObjectOntologyMapperImpl(UnitOfWorkImpl uow, Connection connection) {
		this.uow = Objects.requireNonNull(uow);
		this.storageConnection = Objects.requireNonNull(connection);
		this.metamodel = uow.getMetamodel();
		this.descriptorFactory = new AxiomDescriptorFactory();
		this.instanceRegistry = new InstanceRegistry();
		this.pendingPersists = new PendingChangeRegistry();
		this.entityBuilder = new EntityConstructor(this);
		this.entityBreaker = new EntityDeconstructor(this);
	}

	@Override
	public <T> T loadEntity(Class<T> cls, URI primaryKey, Descriptor descriptor) {
		assert cls != null;
		assert primaryKey != null;
		assert descriptor != null;

		instanceRegistry.reset();
		return loadEntityInternal(cls, primaryKey, descriptor);
	}

	private <T> T loadEntityInternal(Class<T> cls, URI primaryKey, Descriptor descriptor) {
		final EntityType<T> et = getEntityType(cls);
		final AxiomDescriptor axiomDescriptor = descriptorFactory.createForEntityLoading(
				primaryKey, descriptor, et);
		try {
			final Collection<Axiom<?>> axioms = storageConnection.find(axiomDescriptor);
			if (axioms.isEmpty()) {
				return null;
			}
			return entityBuilder.reconstructEntity(primaryKey, et, descriptor, axioms);
		} catch (OntoDriverException e) {
			throw new StorageAccessException(e);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new EntityReconstructionException(e);
		}
	}

	public <T> EntityType<T> getEntityType(Class<T> cls) {
		return (EntityType<T>) metamodel.entity(cls);
	}

	@Override
	public <T> void loadFieldValue(T entity, Field field, Descriptor descriptor) {
		assert entity != null;
		assert field != null;
		assert descriptor != null;

		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Lazily loading value of field " + field + " of entity " + entity);
		}

		final URI primaryKey = EntityPropertiesUtils.getPrimaryKey(entity, metamodel).toURI();
		final EntityType<T> et = (EntityType<T>) getEntityType(entity.getClass());
		final AxiomDescriptor axiomDescriptor = descriptorFactory.createForFieldLoading(primaryKey,
				field, descriptor, et);
		try {
			final Collection<Axiom<?>> axioms = storageConnection.find(axiomDescriptor);
			if (axioms.isEmpty()) {
				return;
			}
			entityBuilder.setFieldValue(entity, field, axioms, et, descriptor);
		} catch (OntoDriverException e) {
			throw new StorageAccessException(e);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new EntityReconstructionException(e);
		}
	}

	@Override
	public <T> void persistEntity(URI primaryKey, T entity, Descriptor descriptor) {
		assert entity != null;
		assert descriptor != null;

		final EntityType<T> et = (EntityType<T>) getEntityType(entity.getClass());
		try {
			if (primaryKey == null) {
				primaryKey = generateIdentifier(et);
				assert primaryKey != null;
				EntityPropertiesUtils.setPrimaryKey(primaryKey, entity, et);
			}
			entityBreaker.setCascadeResolver(new PersistCascadeResolver(this));
			final MutationAxiomDescriptor axiomDescriptor = entityBreaker.mapEntityToAxioms(
					primaryKey, entity, et, descriptor);
			storageConnection.persist(axiomDescriptor);
			pendingPersists.removeInstance(primaryKey, descriptor.getContext());
		} catch (IllegalArgumentException e) {
			throw new EntityDeconstructionException("Unable to deconstruct entity " + entity, e);
		} catch (OntoDriverException e) {
			throw new StorageAccessException(e);
		}
	}

	public URI generateIdentifier(EntityType<?> et) {
		try {
			return storageConnection.generateIdentifier(et.getIRI().toURI());
		} catch (OntoDriverException e) {
			throw new StorageAccessException(e);
		}
	}

	public <T> T getEntityFromCacheOrOntology(Class<T> cls, URI primaryKey, Descriptor descriptor) {
		if (uow.getLiveObjectCache().contains(cls, primaryKey, descriptor.getContext())) {
			return uow.getLiveObjectCache().get(cls, primaryKey, descriptor.getContext());
		} else if (instanceRegistry.containsInstance(primaryKey, descriptor.getContext())) {
			// This prevents endless cycles in bidirectional relationships
			return instanceRegistry.getInstance(primaryKey, descriptor.getContext());
		} else {
			return loadEntityInternal(cls, primaryKey, descriptor);
		}
	}

	<T> void registerInstance(URI primaryKey, T instance, URI context) {
		instanceRegistry.registerInstance(primaryKey, instance, context);
	}

	@Override
	public void checkForUnpersistedChanges() {
		try {
			final Map<URI, Map<URI, Object>> persists = pendingPersists.getInstances();
			if (!persists.isEmpty()) {
				for (URI ctx : persists.keySet()) {
					for (Entry<URI, Object> e : persists.get(ctx).entrySet()) {
						doesInstanceExistInOntology(ctx, e.getKey(), e.getValue());
					}
				}
			}
		} catch (OntoDriverException e) {
			throw new StorageAccessException(e);
		}
	}

	private void doesInstanceExistInOntology(URI ctx, URI primaryKey, Object instance)
			throws OntoDriverException {
		final AxiomDescriptor d = new AxiomDescriptor(NamedResource.create(primaryKey));
		final Assertion typeAssertion = Assertion.createClassAssertion(false);
		d.addAssertion(typeAssertion);
		d.setAssertionContext(typeAssertion, ctx);
		final Collection<Axiom<?>> res = storageConnection.find(d);
		if (!containsClassAssertion(instance, res)) {
			throw new UnpersistedChangeException(
					"Encountered an instance that was neither persisted nor marked as cascade for persist. The instance: "
							+ instance);
		}
	}

	private boolean containsClassAssertion(Object value, Collection<Axiom<?>> res) {
		final EntityType<?> et = getEntityType(value.getClass());
		for (Axiom<?> ax : res) {
			if (MappingUtils.isEntityClassAssertion(ax, et)) {
				return true;
			}
		}
		return false;
	}

	boolean doesInstanceExist(Object entity) {
		return uow.contains(entity);
	}

	<T> void registerPendingPersist(URI primaryKey, T entity, URI context) {
		pendingPersists.registerInstance(primaryKey, entity, context);
	}

	@Override
	public <T> void removeEntity(URI primaryKey, Class<T> cls, Descriptor descriptor) {
		final EntityType<T> et = getEntityType(cls);
		final AxiomDescriptor axiomDescriptor = descriptorFactory.createForEntityLoading(
				primaryKey, descriptor, et);
		try {
			storageConnection.remove(axiomDescriptor);
		} catch (OntoDriverException e) {
			throw new StorageAccessException("Exception caught when removing entity.", e);
		}
	}
}
