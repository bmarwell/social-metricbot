package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.annotation.JsonbProperty;

public record AtProtoLoginBody(
        @JsonbProperty("identifier") String identifier, @JsonbProperty("password") char[] password) {}
