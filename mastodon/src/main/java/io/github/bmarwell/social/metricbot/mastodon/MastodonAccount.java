package io.github.bmarwell.social.metricbot.mastodon;

public record MastodonAccount(MastodonAccountId id, String acct, String username, boolean locked) {}
