package io.github.bmarwell.social.metricbot.bsky;

import io.github.bmarwell.social.metricbot.bsky.json.AtNotificationAuthor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record BskyStatus(
        String uri,
        String cid,
        AtNotificationAuthor author,
        String text,
        RecordType type,
        List<String> lang,
        Instant createdAt,
        Optional<String> inReplyTo) {

    public boolean isReply() {
        return inReplyTo().isPresent();
    }
}
