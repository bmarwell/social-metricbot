package io.github.bmarwell.twitter.metricbot.mastodon;

public record MastodonStatusId(String value) {

    public static MastodonStatusId empty() {
        return new MastodonStatusId("");
    }
}
