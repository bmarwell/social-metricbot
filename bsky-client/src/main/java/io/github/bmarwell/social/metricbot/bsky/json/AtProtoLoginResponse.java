package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.annotation.JsonbProperty;

public record AtProtoLoginResponse(
        @JsonbProperty("handle") String handle,
        @JsonbProperty("accessJwt") String accessJwt,
        @JsonbProperty("refreshJwt") String refreshJwt) {}
