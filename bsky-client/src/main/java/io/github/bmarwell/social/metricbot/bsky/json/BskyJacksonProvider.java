package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum BskyJacksonProvider {
    INSTANCE;

    private final ObjectMapper objectMapper;

    BskyJacksonProvider() {
        this.objectMapper =
                new ObjectMapper().findAndRegisterModules().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
