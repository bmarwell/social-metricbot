package io.github.bmarwell.social.metricbot.bsky.json;

import io.github.bmarwell.social.metricbot.bsky.RecordType;
import jakarta.json.bind.annotation.JsonbProperty;
import java.time.Instant;
import java.util.List;

public record AtPostNotificationRecord(
        @JsonbProperty("text") String text,
        @JsonbProperty("$type") RecordType type,
        @JsonbProperty("lang") List<String> lang,
        @JsonbProperty("createdAt") Instant createdAt)
        implements AtNotificationRecord {}
