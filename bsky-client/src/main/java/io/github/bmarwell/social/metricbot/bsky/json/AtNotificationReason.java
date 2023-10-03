package io.github.bmarwell.social.metricbot.bsky.json;

import java.util.Arrays;

public enum AtNotificationReason {
    FOLLOW("follow"),
    REPOST("repost"),
    LIKE("like"),
    MENTION("mention"),
    UNKNOWN("unknown");

    private final String reasonString;

    AtNotificationReason(final String reasonString) {
        this.reasonString = reasonString;
    }

    public String getReasonString() {
        return reasonString;
    }

    public static AtNotificationReason fromString(final String reasonString) {
        return Arrays.stream(AtNotificationReason.values())
                .filter(anr -> anr.getReasonString().equals(reasonString))
                .findFirst()
                .orElse(AtNotificationReason.UNKNOWN);
    }
}
