package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.annotation.JsonbProperty;

public record AtProtoLoginBody(
        @JsonbProperty("identifier") String identifier, @JsonbProperty("password") String password) {
    public static AtProtoLoginBody from(final String handle, final char[] appSecret) {
        return new AtProtoLoginBody(handle, new String(appSecret));
    }
}
