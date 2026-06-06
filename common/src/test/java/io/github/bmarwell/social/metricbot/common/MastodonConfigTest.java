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
package io.github.bmarwell.social.metricbot.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MastodonConfigTest {

    MastodonConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = new MastodonConfig();
        setField("instanceHostname", Optional.empty());
        setField("accessToken", Optional.empty());
        setField("accountName", Optional.empty());
        setField("website", Optional.empty());
    }

    @Test
    void isConfigured_returnsFalse_whenAllBlank() {
        assertThat(config.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsFalse_whenOnlyInstanceHostname() throws Exception {
        setField("instanceHostname", Optional.of("mastodon.social"));

        assertThat(config.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsFalse_whenAccountNameBlank() throws Exception {
        setField("instanceHostname", Optional.of("mastodon.social"));
        setField("accessToken", Optional.of("my-token"));

        assertThat(config.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsTrue_whenAllThreeSet() throws Exception {
        setField("instanceHostname", Optional.of("mastodon.social"));
        setField("accessToken", Optional.of("my-token"));
        setField("accountName", Optional.of("bot@mastodon.social"));

        assertThat(config.isConfigured()).isTrue();
    }

    @Test
    void getInstanceHostname_prependsHttps_whenNoScheme() throws Exception {
        setField("instanceHostname", Optional.of("mastodon.social"));

        assertThat(config.getInstanceHostname()).isEqualTo("https://mastodon.social");
    }

    @Test
    void getInstanceHostname_returnsBlank_whenAbsent() {
        assertThat(config.getInstanceHostname()).isBlank();
    }

    @Test
    void getInstanceHostname_doesNotPrepend_whenAlreadyHttps() throws Exception {
        setField("instanceHostname", Optional.of("https://mastodon.social"));

        assertThat(config.getInstanceHostname()).isEqualTo("https://mastodon.social");
    }

    @Test
    void getAccountName_returnsValue_whenSet() throws Exception {
        setField("accountName", Optional.of("bot@mastodon.social"));

        assertThat(config.getAccountName()).isEqualTo("bot@mastodon.social");
    }

    @Test
    void getAccessToken_returnsValue_whenSet() throws Exception {
        setField("accessToken", Optional.of("my-token"));

        assertThat(config.getAccessToken()).isEqualTo("my-token");
    }

    @Test
    void getWebsite_returnsValue_whenSet() throws Exception {
        setField("website", Optional.of("https://mastodon.social/@bot"));

        assertThat(config.getWebsite()).isEqualTo("https://mastodon.social/@bot");
    }

    @Test
    void getRedirectUri_returnsOobUri() {
        assertThat(config.getRedirectUri()).isEqualTo("urn:ietf:wg:oauth:2.0:oob");
    }

    @Test
    void getTweetFinderInitialDelay_returnsConfiguredSeconds() throws Exception {
        setField("initialDelay", 60L);

        assertThat(config.getTweetFinderInitialDelay().getSeconds()).isEqualTo(60L);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = MastodonConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(config, value);
    }
}
