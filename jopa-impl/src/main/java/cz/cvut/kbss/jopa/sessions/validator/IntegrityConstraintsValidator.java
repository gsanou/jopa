package cz.cvut.kbss.jopa.sessions.validator;

import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.metamodel.Attribute;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.sessions.ChangeRecord;
import cz.cvut.kbss.jopa.sessions.ObjectChangeSet;
import cz.cvut.kbss.jopa.utils.ErrorUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
     * Validates integrity constraints of all fields of the specified instance.
     *
     * @param instance The instance to validate
     */
    public void validate(Object instance) {
        Objects.requireNonNull(instance, ErrorUtils.constructNPXMessage("instance"));
        final Class<?> cls = instance.getClass();
        for (Field f : cls.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            try {
                final Object value = f.get(instance);
                validate(f, value);
            } catch (IllegalAccessException e) {
                throw new OWLPersistenceException(e);
            }
        }
    }

    /**
     * Validates integrity constraints of all attributes of the specified instance.
     *
     * @param instance The instance to validate
     * @param et       EntityType of the instance
     * @param skipLazy Whether to skip validation of lazily loaded attributes
     * @param <T>      Entity class
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
                validate(f, value);
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
    public void validate(ObjectChangeSet changeSet) {
        Objects.requireNonNull(changeSet, ErrorUtils.constructNPXMessage("changeSet"));
        for (Map.Entry<String, ChangeRecord> entry : changeSet.getChanges().entrySet()) {
            try {
                final Field field = changeSet.getObjectClass().getDeclaredField(entry.getKey());
                validate(field, entry.getValue().getNewValue());
            } catch (NoSuchFieldException e) {
                throw new OWLPersistenceException("Fatal error: field " + entry.getKey() + " not found in entity "
                        + changeSet.getObjectClass());
            }
        }
    }

    /**
     * Validates integrity constraints against the value and of the specified.
     *
     * @param field      Field on which the constraints are defined
     * @param fieldValue The field value to validate
     */
    public abstract void validate(Field field, Object fieldValue);

    /**
     * Validates whether the specified value conforms to the attribute integrity constraints.
     *
     * @param attribute      Attribute metadata with integrity constraints
     * @param attributeValue Value to be validated
     */
    public abstract void validate(Attribute<?, ?> attribute, Object attributeValue);
}
