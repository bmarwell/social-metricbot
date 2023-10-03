package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.bmarwell.social.metricbot.bsky.RecordType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(name = "app.bsky.feed.post", value = AtPostNotificationRecord.class),
    @JsonSubTypes.Type(name = "app.bsky.graph.follow", value = AtNotificationFollowRecord.class),
    @JsonSubTypes.Type(name = "app.bsky.feed.like", value = AtNotificationLikeRecord.class),
    @JsonSubTypes.Type(name = "app.bsky.feed.repost", value = AtNotificationRepostRecord.class)
})
public sealed interface AtNotificationRecord
        permits AtNotificationFollowRecord,
                AtNotificationLikeRecord,
                AtPostNotificationRecord,
                AtNotificationRepostRecord {
    @JsonDeserialize(converter = RecordTypeAdapter.class)
    @JsonProperty("$type")
    RecordType type();
}
