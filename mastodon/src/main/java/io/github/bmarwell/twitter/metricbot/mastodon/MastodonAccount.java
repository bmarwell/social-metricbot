package io.github.bmarwell.twitter.metricbot.mastodon;

public record MastodonAccount(MastodonAccountId id, String acct, String username, boolean locked) {}
