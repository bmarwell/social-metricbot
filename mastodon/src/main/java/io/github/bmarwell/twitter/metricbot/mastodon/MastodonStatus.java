package io.github.bmarwell.twitter.metricbot.mastodon;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record MastodonStatus(
        MastodonStatusId id,
        Optional<MastodonStatusId> inReplyToId,
        long favouritesCount,
        String htmlContent,
        String rawContent,
        URI uri,
        URI url,
        Instant createdAt,
        List<MastodonMention> mentions,
        MastodonAccount account,
        boolean isReblogged,
        Optional<MastodonStatus> reblogged) {}
