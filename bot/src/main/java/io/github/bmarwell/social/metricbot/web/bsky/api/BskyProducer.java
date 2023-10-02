package io.github.bmarwell.social.metricbot.web.bsky.api;

import io.github.bmarwell.social.metricbot.bsky.BlueSkyClient;
import io.github.bmarwell.social.metricbot.bsky.BskyClientFactory;
import io.github.bmarwell.social.metricbot.bsky.MutableBlueSkyConfiguration;
import io.github.bmarwell.social.metricbot.common.BlueSkyBotConfig;
import io.github.bmarwell.social.metricbot.web.factory.MastodonProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.io.Serial;
import java.io.Serializable;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Default
public class BskyProducer implements Serializable {

    @Serial
    private static final long serialVersionUID = 5177043145066730288L;

    private static final Jsonb JSONB = JsonbBuilder.newBuilder().build();

    @Inject
    BlueSkyBotConfig bskyConfig;

    @Produces
    public BlueSkyClient produceBlueSky() {
        final var bskyClientConfig = new MutableBlueSkyConfiguration()
                .withHandle(this.bskyConfig.getHandle())
                .withAppSecret(this.bskyConfig.getAppSecret());

        return new BskyClientFactory(bskyClientConfig).build();
    }

    public void disposeClient(@Disposes final BlueSkyClient bskyClient) {
        try {
            bskyClient.close();
            JSONB.close();
        } catch (final Exception e) {
            LoggerFactory.getLogger(MastodonProducer.class).error("Unable to close JSONB: " + JSONB);
        }
    }
}
