package io.github.bmarwell.twitter.metricbot.web.mastodon;

import io.github.bmarwell.twitter.metricbot.mastodon.MastodonStatus;

public record MastodonProcessRequest(MastodonStatus status) {}