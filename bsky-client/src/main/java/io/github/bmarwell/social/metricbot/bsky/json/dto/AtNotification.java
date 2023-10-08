package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "reason")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AtMentionNotification.class, name = "mention"),
    @JsonSubTypes.Type(value = AtFollowNotification.class, name = "follow"),
    @JsonSubTypes.Type(value = AtLikeNotification.class, name = "like"),
    @JsonSubTypes.Type(value = AtRepostNotification.class, name = "repost"),
    @JsonSubTypes.Type(value = AtQuoteNotification.class, name = "quote"),
})
public sealed interface AtNotification
        permits AtFollowNotification,
                AtLikeNotification,
                AtMentionNotification,
                AtQuoteNotification,
                AtRepostNotification {
    @JsonDeserialize(converter = AtNotificationReasonAdapter.class)
    @JsonProperty("reason")
    AtNotificationReason reason();
}
