package io.github.bmarwell.twitter.metricbot.web.factory;

import io.github.bmarwell.twitter.metricbot.common.MastodonConfig;
import io.github.bmarwell.twitter.metricbot.mastodon.MastodonClient;
import io.github.bmarwell.twitter.metricbot.mastodon.MastodonConfigurationBuilder;
import io.github.bmarwell.twitter.metricbot.mastodon.MastodonFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.net.URI;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MastodonProducer {

    private static final Jsonb JSONB = JsonbBuilder.newBuilder().build();

    @Inject
    MastodonConfig mastodonConfig;

    @Produces
    public MastodonClient produceMastodon() {
        MastodonConfigurationBuilder configurationBuilder = new MastodonConfigurationBuilder()
                .withInstanceHost(URI.create(mastodonConfig.getInstanceHostname()))
                .withAccessToken(mastodonConfig.getAccessToken());

        return new MastodonFactory(configurationBuilder).build();
    }

    public void disposeClient(@Disposes MastodonClient mastodonClient) {
        try {
            mastodonClient.close();
            JSONB.close();
        } catch (Exception e) {
            LoggerFactory.getLogger(MastodonProducer.class).error("Unable to close JSONB: " + JSONB);
        }
    }
}
