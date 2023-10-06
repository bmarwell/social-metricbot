package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AtNotificationResponseWrapper(@JsonProperty("notifications") List<AtNotification> notifications) {}
