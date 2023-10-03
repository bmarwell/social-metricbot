package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;

public record AtNotificationResponseWrapper(@JsonbProperty("notifications") List<AtNotification> notifications) {}
