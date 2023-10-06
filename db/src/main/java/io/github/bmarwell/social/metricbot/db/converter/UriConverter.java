package io.github.bmarwell.social.metricbot.db.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.net.URI;

@Converter(autoApply = true)
public class UriConverter implements AttributeConverter<URI, String> {
    @Override
    public String convertToDatabaseColumn(final URI attribute) {
        return attribute.toString();
    }

    @Override
    public URI convertToEntityAttribute(final String dbData) {
        return URI.create(dbData);
    }
}
