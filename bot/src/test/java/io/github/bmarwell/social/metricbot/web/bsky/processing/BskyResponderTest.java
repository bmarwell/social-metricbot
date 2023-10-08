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
