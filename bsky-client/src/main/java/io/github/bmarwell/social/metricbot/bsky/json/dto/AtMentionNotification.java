package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public record AtMentionNotification(
        @JsonProperty("uri") URI uri,
        @JsonProperty("cid") String cid,
        @JsonProperty("author") AtNotificationAuthor author,
        @JsonDeserialize(converter = AtNotificationReasonAdapter.class) @JsonProperty("reason")
                AtNotificationReason reason,
        @JsonProperty("record") AtPostNotificationRecord record,
        @JsonProperty("indexedAt") Instant indexedAt,
        @JsonProperty("isRead") boolean isRead,
        @JsonProperty("embed") Optional<AtEmbed> embed)
        implements AtNotification {}
