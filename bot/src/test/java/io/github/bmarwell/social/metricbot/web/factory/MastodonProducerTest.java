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
package io.github.bmarwell.social.metricbot.web.factory;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import io.github.bmarwell.social.metricbot.common.MastodonConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MastodonProducerTest {

    @Mock
    MastodonConfig mastodonConfig;

    @InjectMocks
    MastodonProducer producer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void produceMastodon_throwsIllegalStateException_whenNotConfigured() {
        when(mastodonConfig.isConfigured()).thenReturn(false);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> producer.produceMastodon())
                .withMessageContaining("accountname")
                .withMessageContaining("instancehostname")
                .withMessageContaining("accesstoken");
    }
}
