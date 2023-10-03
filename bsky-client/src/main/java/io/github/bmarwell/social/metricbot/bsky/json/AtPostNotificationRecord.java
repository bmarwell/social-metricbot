package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.bmarwell.social.metricbot.bsky.RecordType;
import java.time.Instant;
import java.util.List;

public record AtPostNotificationRecord(
        @JsonProperty("text") String text,
        @JsonDeserialize(converter = RecordTypeAdapter.class) @JsonProperty("$type") RecordType type,
        @JsonProperty("lang") List<String> lang,
        @JsonProperty("createdAt") Instant createdAt)
        implements AtNotificationRecord {}
