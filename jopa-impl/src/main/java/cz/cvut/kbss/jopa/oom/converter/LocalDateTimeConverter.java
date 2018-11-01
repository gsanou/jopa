package cz.cvut.kbss.jopa.oom.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Converts between Java 8 {@link LocalDateTime} and {@link Date} used by OntoDriver and the underlying repository
 * access frameworks (RDF4J, Jena, OWLAPI).
 */
public class LocalDateTimeConverter implements ConverterWrapper<LocalDateTime, Date> {

    @Override
    public Date convertToAxiomValue(LocalDateTime value) {
        return value != null ? java.sql.Timestamp.valueOf(value) : null;
    }

    @Override
    public LocalDateTime convertToAttribute(Date value) {
        return value != null ? value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    @Override
    public boolean supportsAxiomValueType(Class<?> type) {
        return Date.class.isAssignableFrom(type);
    }
}