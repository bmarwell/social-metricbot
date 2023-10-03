package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.bmarwell.social.metricbot.bsky.RecordType;

public record AtNotificationLikeRecord(@JsonProperty("$type") RecordType type) implements AtNotificationRecord {}
