package io.github.bmarwell.social.metricbot.mastodon;

public class MastodonFactory {

    private final MastodonConfigurationBuilder config;

    public MastodonFactory(MastodonConfigurationBuilder mcb) {
        this.config = mcb;
    }

    public MastodonClient build() {
        return new DefaultMastodonClient(this.config);
    }
}
