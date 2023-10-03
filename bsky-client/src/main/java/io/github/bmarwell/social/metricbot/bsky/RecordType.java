package io.github.bmarwell.social.metricbot.bsky;

import java.util.Arrays;

public enum RecordType {
    POST("app.bsky.feed.post"),
    LIKE("app.bsky.feed.like"),
    REPOST("app.bsky.feed.repost"),
    FOLLOW("app.bsky.graph.follow"),
    UNKNOWN("unknown");

    private final String typeId;

    RecordType(final String typeId) {
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }

    public static RecordType fromString(final String typeId) {
        return Arrays.stream(RecordType.values())
                .filter(pt -> pt.getTypeId().equals(typeId))
                .findFirst()
                .orElse(RecordType.UNKNOWN);
    }
}
