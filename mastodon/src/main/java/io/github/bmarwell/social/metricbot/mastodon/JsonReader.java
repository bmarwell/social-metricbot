package io.github.bmarwell.social.metricbot.mastodon;

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

    public JsonReader(Jsonb jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public T readFrom(
            Class<T> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream)
            throws IOException, WebApplicationException {
        try {
            return this.jsonb.fromJson(entityStream, type);
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
