package io.github.bmarwell.social.metricbot.bsky.json;

import io.github.bmarwell.social.metricbot.bsky.RecordType;
import jakarta.json.bind.annotation.JsonbProperty;

public record AtNotificationFollowRecord(@JsonbProperty("$type") RecordType type) implements AtNotificationRecord {}
