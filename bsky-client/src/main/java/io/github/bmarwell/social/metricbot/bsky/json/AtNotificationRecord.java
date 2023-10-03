package io.github.bmarwell.social.metricbot.bsky.json;

import io.github.bmarwell.social.metricbot.bsky.RecordType;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.json.bind.annotation.JsonbTypeInfo;

@JsonbTypeInfo(
        key = "$type",
        value = {
            @JsonbSubtype(alias = "app.bsky.feed.post", type = AtPostNotificationRecord.class),
            @JsonbSubtype(alias = "app.bsky.graph.follow", type = AtNotificationFollowRecord.class),
            @JsonbSubtype(alias = "app.bsky.feed.like", type = AtNotificationLikeRecord.class),
            @JsonbSubtype(alias = "app.bsky.feed.repost", type = AtNotificationRepostRecord.class)
        })
public sealed interface AtNotificationRecord
        permits AtNotificationFollowRecord,
                AtNotificationLikeRecord,
                AtPostNotificationRecord,
                AtNotificationRepostRecord {
    @JsonbTypeAdapter(RecordTypeAdapter.class)
    @JsonbProperty("$type")
    RecordType type();
}
