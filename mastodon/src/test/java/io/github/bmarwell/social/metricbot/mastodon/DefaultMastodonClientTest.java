/*
 * Copyright 2023-2026 The social-metricbot contributors
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
package io.github.bmarwell.social.metricbot.mastodon;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class DefaultMastodonClientTest {

    @RegisterExtension
    static final WireMockExtension WIREMOCK = WireMockExtension.newInstance().build();

    @Test
    void getRecentMentions_throwsIllegalStateException_when404() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v1/notifications"))
                .willReturn(aResponse().withStatus(404)));

        MastodonConfigurationBuilder config = new MastodonConfigurationBuilder()
                .withInstanceHost(URI.create(WIREMOCK.baseUrl()))
                .withAccessToken("test-token");

        try (DefaultMastodonClient client = new DefaultMastodonClient(config)) {
            assertThatExceptionOfType(ExecutionException.class)
                    .isThrownBy(() ->
                            client.getRecentMentions().toCompletableFuture().get())
                    .withCauseInstanceOf(IllegalStateException.class);
        }
    }
}
