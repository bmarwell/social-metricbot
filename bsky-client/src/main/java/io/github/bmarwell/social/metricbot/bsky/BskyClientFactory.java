package io.github.bmarwell.social.metricbot.bsky;

public class BskyClientFactory {

    private final MutableBlueSkyConfiguration bskyConfig;

    public BskyClientFactory(final MutableBlueSkyConfiguration bskyConfig) {
        this.bskyConfig = bskyConfig.clone();
    }

    public BlueSkyClient build() {
        return new DefaultBlueSkyClient(this.bskyConfig);
    }
}
