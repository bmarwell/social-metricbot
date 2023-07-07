package io.github.bmarwell.twitter.metricbot.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MastodonConfig implements Serializable {

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.twitter.metricbot.mastodon.accountname")
    private String accountName;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.twitter.metricbot.mastodon.instancehostname")
    private String instanceHostname;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.twitter.metricbot.mastodon.website")
    private String website;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.twitter.metricbot.mastodon.accesstoken")
    private String accessToken;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.twitter.metricbot.mastodon.initialDelay")
    private long initialDelay;

    public MastodonConfig() {
        // injection constructor.
    }

    public String getAccountName() {
        return accountName;
    }

    public String getInstanceHostname() {
        if (!instanceHostname.startsWith("http")) {
            return "https://" + instanceHostname;
        }
        return instanceHostname;
    }

    public String getWebsite() {
        return website;
    }

    public String getRedirectUri() {
        return "urn:ietf:wg:oauth:2.0:oob";
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public Duration getTweetFinderInitialDelay() {
        return Duration.ofSeconds(this.initialDelay);
    }
}
