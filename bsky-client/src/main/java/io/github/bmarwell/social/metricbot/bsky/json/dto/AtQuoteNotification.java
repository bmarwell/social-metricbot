package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public record AtQuoteNotification(
        @JsonDeserialize(converter = AtNotificationReasonAdapter.class) @JsonProperty("reason")
                AtNotificationReason reason)
        implements AtNotification {}
