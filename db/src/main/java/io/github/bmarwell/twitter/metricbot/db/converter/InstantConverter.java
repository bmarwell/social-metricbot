package io.github.bmarwell.twitter.metricbot.db.converter;

import java.time.Instant;
import java.util.Date;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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
