package cz.cvut.kbss.jopa.sessions;

import java.net.URI;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import cz.cvut.kbss.jopa.model.Repository;
import cz.cvut.kbss.jopa.model.EntityDescriptor;

final class RepositoryMap {

	private final Map<Integer, Map<URI, Map<Object, Object>>> map;
	private Map<Object, EntityDescriptor> entityToRepository;

	public RepositoryMap(List<Repository> repos) {
		final int size = repos.size();
		this.map = new HashMap<>(size);
		for (Repository r : repos) {
			map.put(r.getId(), new HashMap<URI, Map<Object, Object>>());
		}
	}

	void initEntityToRepository() {
		this.entityToRepository = new IdentityHashMap<>();
	}

	void add(EntityDescriptor repository, Object key, Object value) {
		assert repository != null;
		assert !repository.getContexts().isEmpty();
		assert key != null;
		// Null values are permitted

		final Map<Object, Object> entities = getMap(repository);
		entities.put(key, value);
	}

	void remove(EntityDescriptor repository, Object key) {
		assert repository != null;
		assert !repository.getContexts().isEmpty();
		assert repository.getRepository() < map.size();
		assert key != null;

		final Map<Object, Object> entities = getMap(repository);
		entities.remove(key);
	}

	void addEntityToRepository(Object entity, EntityDescriptor repository) {
		assert entityToRepository != null;
		entityToRepository.put(entity, repository);
	}

	void removeEntityToRepository(Object entity) {
		assert entityToRepository != null;
		entityToRepository.remove(entity);
	}

	boolean contains(EntityDescriptor repository, Object key) {
		assert repository != null;
		assert !repository.getContexts().isEmpty();
		assert repository.getRepository() < map.size();
		assert key != null;

		final Map<Object, Object> entities = getMap(repository);
		return entities.containsKey(key);
	}

	Object get(EntityDescriptor repository, Object key) {
		assert repository != null;
		assert !repository.getContexts().isEmpty();
		assert repository.getRepository() < map.size();
		assert key != null;

		final Map<Object, Object> entities = getMap(repository);
		if (!entities.containsKey(key)) {
			return null;
		}
		return entities.get(key);
	}

	EntityDescriptor getRepositoryID(Object entity) {
		assert entityToRepository != null;
		assert entity != null;

		return entityToRepository.get(entity);
	}

	void clear() {
		for (Map<URI, Map<Object, Object>> m : map.values()) {
			m.clear();
		}
		if (entityToRepository != null) {
			initEntityToRepository();
		}
	}

	private Map<Object, Object> getMap(EntityDescriptor repository) {
		final Map<URI, Map<Object, Object>> m = map.get(repository.getRepository());
		if (m == null) {
			throw new IllegalArgumentException("Unknown repository " + repository);
		}
		final URI ctx = repository.getContexts().iterator().next();
		Map<Object, Object> entities;
		if (!m.containsKey(ctx)) {
			entities = new HashMap<>();
			m.put(ctx, entities);
		} else {
			entities = m.get(ctx);
		}
		return entities;
	}
}
