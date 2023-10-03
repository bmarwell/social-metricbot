package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;

public record AtMentionNotification(
        @JsonProperty("uri") String uri,
        @JsonProperty("cid") String cid,
        @JsonProperty("author") AtNotificationAuthor author,
        @JsonDeserialize(converter = AtNotificationReasonAdapter.class) @JsonProperty("reason")
                AtNotificationReason reason,
        @JsonProperty("record") AtPostNotificationRecord record,
        @JsonProperty("indexedAt") Instant indexedAt,
        @JsonProperty("isRead") boolean isRead)
        implements AtNotification {}
