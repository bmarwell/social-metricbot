package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AtProtoLoginResponse(
        @JsonProperty("handle") String handle,
        @JsonProperty("accessJwt") String accessJwt,
        @JsonProperty("refreshJwt") String refreshJwt) {}
