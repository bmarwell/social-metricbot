package io.github.bmarwell.twitter.metricbot.mastodon;

import java.net.URI;

public record MastodonMention(MastodonAccountId id, URI url, String accountName, String userName) {}
