package io.github.bmarwell.twitter.metricbot.mastodon;

public record MastodonTextStatusDraft(
        String tootText,
        MastodonStatusId replyToId,
        MastodonStatusVisiblilty visibility,
        MastodonStatusLanguage language) {}
