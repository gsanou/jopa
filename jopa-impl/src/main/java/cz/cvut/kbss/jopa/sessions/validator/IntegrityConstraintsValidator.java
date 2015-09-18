package cz.cvut.kbss.jopa.sessions.validator;

import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.metamodel.Attribute;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.FieldSpecification;
import cz.cvut.kbss.jopa.model.metamodel.Metamodel;
import cz.cvut.kbss.jopa.sessions.ChangeRecord;
import cz.cvut.kbss.jopa.sessions.ObjectChangeSet;
import cz.cvut.kbss.jopa.utils.ErrorUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public abstract class IntegrityConstraintsValidator {

    private static IntegrityConstraintsValidator generalValidator = initGeneralValidator();

    private static IntegrityConstraintsValidator initGeneralValidator() {
        final GeneralIntegrityConstraintsValidator validator = new GeneralIntegrityConstraintsValidator();
        validator.addValidator(new CardinalityConstraintsValidator());
        return validator;
    }


    public static IntegrityConstraintsValidator getValidator() {
        return generalValidator;
    }

    /**
     * Validates integrity constraints of all attributes of the specified instance.
     *
     * @param instance The instance to validate
     * @param et       EntityType of the instance
     * @param skipLazy Whether to skip validation of lazily loaded attributes
     * @param <T>      Entity class type
     */
    public <T> void validate(T instance, EntityType<T> et, boolean skipLazy) {
        Objects.requireNonNull(instance, ErrorUtils.constructNPXMessage("instance"));
        Objects.requireNonNull(et, ErrorUtils.constructNPXMessage("et"));

        for (Attribute<T, ?> att : et.getDeclaredAttributes()) {
            if (skipLazy && att.getFetchType() == FetchType.LAZY) {
                continue;
            }
            final Field f = att.getJavaField();
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            try {
                final Object value = f.get(instance);
                validate(att, value);
            } catch (IllegalAccessException e) {
                throw new OWLPersistenceException(e);
            }
        }
    }

    /**
     * Validates integrity constraints for changes in the specified change set.
     *
     * @param changeSet The change set to validate
     */
    public void validate(ObjectChangeSet changeSet, Metamodel metamodel) {
        Objects.requireNonNull(changeSet, ErrorUtils.constructNPXMessage("changeSet"));
        Objects.requireNonNull(metamodel, ErrorUtils.constructNPXMessage("metamodel"));
        for (Map.Entry<String, ChangeRecord> entry : changeSet.getChanges().entrySet()) {
            final EntityType<?> et = metamodel.entity(changeSet.getObjectClass());
            final FieldSpecification<?, ?> fieldSpec = et.getFieldSpecification(entry.getKey());
            validate(fieldSpec, entry.getValue().getNewValue());
        }
    }

    /**
     * Validates whether the specified value conforms to the attribute integrity constraints.
     *
     * @param attribute      Attribute metadata with integrity constraints
     * @param attributeValue Value to be validated
     */
    public abstract void validate(FieldSpecification<?, ?> attribute, Object attributeValue);
}
