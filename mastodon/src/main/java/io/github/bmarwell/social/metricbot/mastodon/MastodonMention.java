package io.github.bmarwell.social.metricbot.mastodon;

import java.net.URI;

public record MastodonMention(MastodonAccountId id, URI url, String accountName, String userName) {}
