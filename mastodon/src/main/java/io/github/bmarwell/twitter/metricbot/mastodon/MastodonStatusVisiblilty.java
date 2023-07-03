package io.github.bmarwell.twitter.metricbot.mastodon;

import java.util.StringJoiner;

public enum MastodonStatusVisiblilty {
    PUBLIC("public"),
    UNLISTED("unlisted"),
    PRIVATE("private"),
    DIRECT("direct");

    private final String value;

    MastodonStatusVisiblilty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MastodonStatusVisiblilty.class.getSimpleName() + "[", "]")
                .add("value='" + value + "'")
                .toString();
    }
}
