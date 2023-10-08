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
package io.github.bmarwell.social.metricbot.web.bsky.processing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.bmarwell.social.metricbot.bsky.BlueSkyClient;
import io.github.bmarwell.social.metricbot.common.BlueSkyBotConfig;
import org.junit.jupiter.api.BeforeEach;

class BskyResponderTest {

    BlueSkyBotConfig botConfig = mock(BlueSkyBotConfig.class);

    BlueSkyClient client = mock(BlueSkyClient.class);

    BskyResponder responder = new BskyResponder();

    @BeforeEach
    void setUp() {
        when(client.getHandle()).then(args -> botConfig.getHandle());
        responder.setBskyClient(client);
    }
}
