package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.Jsonb;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class JsonReader<T> implements MessageBodyReader<T> {

    private final Jsonb jsonb;

    public JsonReader(final Jsonb jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public boolean isReadable(
            final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public T readFrom(
            final Class<T> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream)
            throws IOException, WebApplicationException {
        try {
            return this.jsonb.fromJson(entityStream, type);
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
