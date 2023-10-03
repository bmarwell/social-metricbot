package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.json.bind.annotation.JsonbTypeInfo;

@JsonbTypeInfo(
        key = "reason",
        value = {
            @JsonbSubtype(alias = "mention", type = AtMentionNotification.class),
            @JsonbSubtype(alias = "follow", type = AtFollowNotification.class)
        })
public sealed interface AtNotification permits AtFollowNotification, AtMentionNotification {
    @JsonbTypeAdapter(AtNotificationReasonAdapter.class)
    @JsonbProperty("reason")
    AtNotificationReason reason();
}
