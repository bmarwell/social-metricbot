package io.github.bmarwell.social.metricbot.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Default
public class BlueSkyBotConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = -310024931543855701L;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.bsky.handle")
    private String handle;

    @Inject
    @ConfigProperty(name = "io.github.bmarwell.social.metricbot.bsky.appSecret")
    private String appSecret;

    public BlueSkyBotConfig() {
        // injection only
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(final String handle) {
        this.handle = handle;
    }

    public char[] getAppSecret() {
        return appSecret.toCharArray();
    }

    public void setAppSecret(final String appSecret) {
        this.appSecret = appSecret;
    }

    public Duration getPostFinderInitialDelay() {
        return Duration.ofSeconds(5L);
    }
}
