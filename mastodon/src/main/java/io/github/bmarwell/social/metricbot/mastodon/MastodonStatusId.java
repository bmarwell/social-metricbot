package io.github.bmarwell.social.metricbot.mastodon;

public record MastodonStatusId(String value) {

    public static MastodonStatusId empty() {
        return new MastodonStatusId("");
    }
}
