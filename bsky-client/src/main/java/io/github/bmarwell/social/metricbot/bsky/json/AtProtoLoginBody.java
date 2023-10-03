package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AtProtoLoginBody(
        @JsonProperty("identifier") String identifier, @JsonProperty("password") String password) {
    public static AtProtoLoginBody from(final String handle, final char[] appSecret) {
        return new AtProtoLoginBody(handle, new String(appSecret));
    }
}
