package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

public record AtFollowNotification(
        @JsonbTypeAdapter(AtNotificationReasonAdapter.class) @JsonbProperty("reason") AtNotificationReason reason)
        implements AtNotification {}
