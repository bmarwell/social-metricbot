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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MastodonConfigTest {

    MastodonConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = new MastodonConfig();
        setField("instanceHostname", "");
        setField("accessToken", "");
        setField("accountName", "");
        setField("website", "");
    }

    @Test
    void isConfigured_returnsFalse_whenAllBlank() {
        assertThat(config.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsFalse_whenOnlyInstanceHostname() throws Exception {
        setField("instanceHostname", "mastodon.social");

        assertThat(config.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsFalse_whenAccountNameBlank() throws Exception {
        setField("instanceHostname", "mastodon.social");
        setField("accessToken", "my-token");

        assertThat(config.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsTrue_whenAllThreeSet() throws Exception {
        setField("instanceHostname", "mastodon.social");
        setField("accessToken", "my-token");
        setField("accountName", "bot@mastodon.social");

        assertThat(config.isConfigured()).isTrue();
    }

    @Test
    void getInstanceHostname_prependsHttps_whenNoScheme() throws Exception {
        setField("instanceHostname", "mastodon.social");

        assertThat(config.getInstanceHostname()).isEqualTo("https://mastodon.social");
    }

    @Test
    void getInstanceHostname_returnsBlank_whenBlank() {
        assertThat(config.getInstanceHostname()).isBlank();
    }

    @Test
    void getInstanceHostname_doesNotPrepend_whenAlreadyHttps() throws Exception {
        setField("instanceHostname", "https://mastodon.social");

        assertThat(config.getInstanceHostname()).isEqualTo("https://mastodon.social");
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = MastodonConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(config, value);
    }
}
