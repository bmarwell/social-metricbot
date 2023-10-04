package io.github.bmarwell.social.metricbot.bsky;

import io.github.bmarwell.social.metricbot.bsky.json.AtNotificationAuthor;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record BskyStatus(
        URI uri,
        String cid,
        AtNotificationAuthor author,
        String text,
        RecordType type,
        List<String> lang,
        Instant createdAt,
        Optional<URI> inReplyTo,
        Optional<URI> quotedStatus) {

    public boolean isReply() {
        return inReplyTo().isPresent();
    }

    /**
     * Returns {@code true} if this status quotes (embeds) another post.
     * <p>
     * This is equivalent to {@code quotedStatus().isPresent()}.
     * </p>
     *
     * @return {@code true} if this status quotes (embeds) another post.
     */
    public boolean isQuoting() {
        return quotedStatus().isPresent();
    }
}
