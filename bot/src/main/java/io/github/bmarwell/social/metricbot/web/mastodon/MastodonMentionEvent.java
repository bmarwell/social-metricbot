package io.github.bmarwell.social.metricbot.web.mastodon;

import io.github.bmarwell.social.metricbot.mastodon.MastodonStatus;

public record MastodonMentionEvent(MastodonStatus mastodonStatus) {}