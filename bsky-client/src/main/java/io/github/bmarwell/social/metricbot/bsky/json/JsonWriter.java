package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.Jsonb;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Produces(MediaType.APPLICATION_JSON)
public class JsonWriter<T> implements MessageBodyWriter<T> {

    private final Jsonb jsonb;

    public JsonWriter(final Jsonb jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public boolean isWriteable(
            final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public void writeTo(
            final T t,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream)
            throws IOException, WebApplicationException {
        this.jsonb.toJson(t, entityStream);
    }
}
