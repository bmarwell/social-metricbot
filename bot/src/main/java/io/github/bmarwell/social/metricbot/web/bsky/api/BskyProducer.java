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
    @ApplicationScoped
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
