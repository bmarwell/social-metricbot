package io.github.bmarwell.social.metricbot.bsky;

import io.github.bmarwell.social.metricbot.bsky.json.AtNotificationAuthor;
import java.time.Instant;
import java.util.List;

public record BskyStatus(
        String uri,
        String cid,
        AtNotificationAuthor author,
        String text,
        RecordType type,
        List<String> lang,
        Instant createdAt) {}
