package io.github.bmarwell.twitter.metricbot.db.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.util.Date;

@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, Date> {

    @Override
    public Date convertToDatabaseColumn(final Instant attribute) {
        return Date.from(attribute);
    }

    @Override
    public Instant convertToEntityAttribute(final Date dbData) {
        return dbData.toInstant();
    }
}
