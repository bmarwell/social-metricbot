package io.github.bmarwell.social.metricbot.mastodon;

public record MastodonTextStatusDraft(
        String tootText,
        MastodonStatusId replyToId,
        MastodonStatusVisiblilty visibility,
        MastodonStatusLanguage language) {}
