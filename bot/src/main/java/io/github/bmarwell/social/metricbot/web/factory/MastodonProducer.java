/*
 * Copyright 2023 The social-metricbot contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bmarwell.social.metricbot.web.factory;

import io.github.bmarwell.social.metricbot.common.MastodonConfig;
import io.github.bmarwell.social.metricbot.mastodon.MastodonClient;
import io.github.bmarwell.social.metricbot.mastodon.MastodonConfigurationBuilder;
import io.github.bmarwell.social.metricbot.mastodon.MastodonFactory;
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
