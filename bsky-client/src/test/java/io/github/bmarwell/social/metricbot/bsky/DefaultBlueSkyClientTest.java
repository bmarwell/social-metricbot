package io.github.bmarwell.social.metricbot.bsky;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.bmarwell.social.metricbot.bsky.json.dto.AtNotificationAuthor;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultBlueSkyClientTest {

    MutableBlueSkyConfiguration bsc = new MutableBlueSkyConfiguration().withHandle("metricbot.de");

    DefaultBlueSkyClient client = new DefaultBlueSkyClient(bsc);

    @Test
    void statusUri_contains_profile() {
        // given:
        final var author = new AtNotificationAuthor(
                null, "metricbot.de", "metricbot", "metricbot.de description", Optional.empty(), Instant.now());
        final var status = new BskyStatus(
                URI.create("at://did:plc:dww4ffboffsw3gk7ph4fizpc/app.bsky.feed.post/3kb4fxhzeed25"),
                "abc",
                author,
                "text",
                RecordType.POST,
                List.of("en"),
                Instant.now(),
                Optional.of(URI.create("at://did:plc:iyjavuowe3psaqrxtazsx3xj/app.bsky.feed.post/3kb4fdb4i5m2o")),
                Optional.empty());

        // when:
        final var link = client.getStatusUri(status);

        // then:
        assertThat(link).hasPath("/profile/metricbot.de/post/3kb4fxhzeed25");
    }
}
