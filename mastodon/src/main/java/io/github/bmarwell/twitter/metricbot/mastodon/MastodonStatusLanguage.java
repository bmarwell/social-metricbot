package io.github.bmarwell.twitter.metricbot.mastodon;

import java.util.StringJoiner;

public enum MastodonStatusLanguage {
    ENGLISH("en");

    private final String languageCode;

    MastodonStatusLanguage(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MastodonStatusLanguage.class.getSimpleName() + "[", "]")
                .add("languageCode='" + languageCode + "'")
                .toString();
    }
}
