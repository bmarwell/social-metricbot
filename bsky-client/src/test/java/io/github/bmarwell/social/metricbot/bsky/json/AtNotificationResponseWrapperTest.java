package io.github.bmarwell.social.metricbot.bsky.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class AtNotificationResponseWrapperTest {

    ObjectMapper jsonb = BskyJacksonProvider.INSTANCE.getObjectMapper();

    JsonReader<AtNotificationResponseWrapper> jsonReader = new JsonReader<>();

    @Test
    void can_deserialize() throws IOException {
        // given
        final var notificationResponse =
                """
                {
                  "notifications": [
                    {
                      "uri": "at://did:plc:n5o2wksggcs653t3seg5eu6b/app.bsky.feed.post/3kaqpcklzqg2x",
                      "cid": "bafyreiejmowzo54ffnk4o5ebz3l2jixso6qnezx4h3ctygugp5sjv5z3hu",
                      "author": {
                        "did": "did:plc:n5o2wksggcs653t3seg5eu6b",
                        "handle": "bmarwell.de",
                        "displayName": "Benjamin Marwell",
                        "description": "#JakartaEE-Dev, Musician & Tech-Blogger.\\nUses #Linux.\\n@ApacheShiro PMC.\\n👨‍👩‍👧.\\n🏙️ are my own.",
                        "avatar": "https://av-cdn.bsky.app/img/avatar/plain/did:plc:n5o2wksggcs653t3seg5eu6b/bafkreibjocja63wh6x34jxli2d3ojyyhvckjfvg4sx6rl7kdy3vi5p6juu@jpeg",
                        "indexedAt": "2023-10-02T08:10:24.445Z",
                        "viewer": {
                          "muted": false,
                          "blockedBy": false
                        },
                        "labels": []
                      },
                      "reason": "mention",
                      "record": {
                        "text": "Here on #BlueSky some good bots are still missing. My @metricbot.bsky.social will be ported shortly. On #Twitter he's already gone due to API constraints and quality. It's way easier to develop for BlueSky!",
                        "$type": "app.bsky.feed.post",
                        "langs": [
                          "en"
                        ],
                        "facets": [
                          {
                            "$type": "app.bsky.richtext.facet",
                            "index": {
                              "byteEnd": 76,
                              "byteStart": 54
                            },
                            "features": [
                              {
                                "did": "did:plc:dww4ffboffsw3gk7ph4fizpc",
                                "$type": "app.bsky.richtext.facet#mention"
                              }
                            ]
                          }
                        ],
                        "createdAt": "2023-10-02T05:47:58.002Z"
                      },
                      "isRead": false,
                      "indexedAt": "2023-10-02T05:47:58.002Z",
                      "labels": []
                    }
                  ]
                }
                """;
        final var entityStream = new ByteArrayInputStream(notificationResponse.getBytes(StandardCharsets.UTF_8));

        // when
        final var atNotificationResponse = this.jsonReader.readFrom(
                AtNotificationResponseWrapper.class,
                null,
                null,
                MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedHashMap<>(),
                entityStream);

        // then
        assertThat(atNotificationResponse)
                .isNotNull()
                .extracting(AtNotificationResponseWrapper::notifications)
                .asInstanceOf(InstanceOfAssertFactories.list(AtMentionNotification.class))
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("reason", AtNotificationReason.MENTION)

        // end
        ;
    }
}
