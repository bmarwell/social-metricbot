package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AtPostReply(
        @JsonProperty("root") AtPostReplyInformation root, @JsonProperty("parent") AtPostReplyInformation parent) {}
