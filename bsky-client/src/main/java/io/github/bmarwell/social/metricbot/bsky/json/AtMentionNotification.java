package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import java.time.Instant;

public record AtMentionNotification(
        @JsonbProperty("uri") String uri,
        @JsonbProperty("cid") String cid,
        @JsonbProperty("author") AtNotificationAuthor author,
        @JsonbTypeAdapter(AtNotificationReasonAdapter.class) @JsonbProperty("reason") AtNotificationReason reason,
        @JsonbProperty("record") AtPostNotificationRecord record,
        @JsonbProperty("indexedAt") Instant indexedAt,
        @JsonbProperty("isRead") boolean isRead)
        implements AtNotification {}
