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
package io.github.bmarwell.social.metricbot.bsky;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.bmarwell.social.metricbot.bsky.json.AtLink;
import io.github.bmarwell.social.metricbot.bsky.json.BskyJacksonProvider;
import io.github.bmarwell.social.metricbot.bsky.json.BskyResponseDraft;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtEmbedRecord;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtProtoLoginResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class DefaultBlueSkyClientWiremockIT {

    @RegisterExtension
    public static final WireMockExtension WIREMOCK =
            WireMockExtension.newInstance().build();

    MutableBlueSkyConfiguration bsc = new MutableBlueSkyConfiguration()
                    .withHost(WIREMOCK.baseUrl())
                    .withHandle("mybot.bsky.social")
                    .withAppSecret("abc-def-ghi".toCharArray())
            // end config
            ;

    DefaultBlueSkyClient client = new DefaultBlueSkyClient(bsc);

    @Test
    void login_attempt_can_be_parsed() {
        // given:
        final var accessJwt =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6ImNvbS5hdHByb3RvLmFwcFBhc3MiLCJzdWIiOiJkaWQ6cGxjOmFiY2RlZjEyMzQ1Nnh5ejQ1Njc5OGFhYSIsImlhdCI6MTY5NjYyMTUxMywiZXhwIjoxNjk2NjI4NzEzfQ.duCjej0vChJcrOjBvodLfLpCkyTEbD54TGu62hZ8te8";

        stubLogin(accessJwt);

        // when:
        final var loginResponse = client.doLogin();

        // then
        assertThat(loginResponse)
                // assertions
                .isPresent()
                .get()
                .extracting(AtProtoLoginResponse::handle, AtProtoLoginResponse::accessJwt)
                .contains(this.bsc.getHandle(), accessJwt);
    }

    private void stubLogin(final String accessJwt) {
        WIREMOCK.stubFor(
                post(urlEqualTo("/xrpc/com.atproto.server.createSession"))
                        .withRequestBody(equalToJson(
                                """
                {
                  "identifier": "%s",
                  "password": "%s"
                }
                """
                                        .formatted(this.bsc.getHandle(), String.valueOf(this.bsc.getAppSecret()))))
                        .willReturn(okJson(
                                """
                {
                  "did": "did:plc:abcdef123456xyz456798aaa",
                  "handle": "mybot.bsky.social",
                  "email": "mybot@example.invalid",
                  "emailConfirmed": false,
                  "accessJwt": "%s",
                  "refreshJwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6ImNvbS5hdHByb3RvLnJlZnJlc2giLCJzdWIiOiJkaWQ6cGxjOmFiY2RlZjEyMzQ1Nnh5ejQ1Njc5OGFhYSIsImp0aSI6Ii8iLCJpYXQiOjE2OTY2MjE1MTMsImV4cCI6MTcwNDM5NzUxM30.9W38dQWKyIhoQb1RjxvpahcYkmba4mbfcyMZzn-S50s"
                }
                """
                                        .formatted(accessJwt)))
                // end stub
                );
    }

    @Test
    void facets_and_embed_converted_correctly() throws IOException {
        // given:
        var atUri = URI.create("at://did:plc:dww4ffboffsw3gk7ph4fizpc/app.bsky.feed.post/3kbbbjut3sy2b");
        var statusUri = URI.create("https://bsky.social/profile/metricbot.de/post/3kbbbjut3sy2b");
        var cid = "bafyreibzmwm3yxhi7jibzrmwf6jfygmbtb225vbx5xtllxulc6dtmlilki";

        final BskyStatus reply = new BskyStatus(
                atUri,
                cid,
                null,
                "Bla original text",
                RecordType.POST,
                List.of("en"),
                Instant.now(),
                Optional.empty(),
                Optional.empty());
        var draft = new BskyResponseDraft(
                "Hello, " + statusUri, reply, List.of(new AtLink(statusUri)), Optional.of(new AtEmbedRecord(atUri, cid))
                // end draft
                );

        // stubbed:
        final var accessJwt =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6ImNvbS5hdHByb3RvLmFwcFBhc3MiLCJzdWIiOiJkaWQ6cGxjOmFiY2RlZjEyMzQ1Nnh5ejQ1Njc5OGFhYSIsImlhdCI6MTY5NjYyMTUxMywiZXhwIjoxNjk2NjI4NzEzfQ.duCjej0vChJcrOjBvodLfLpCkyTEbD54TGu62hZ8te8";

        stubLogin(accessJwt);

        WIREMOCK.stubFor(
                post(urlEqualTo("/xrpc/com.atproto.repo.createRecord"))
                        .willReturn(
                                okJson(
                                        """
                        {
                            "uri": "at://u1",
                            "cid": "c1"
                        }
                        """)));
        WIREMOCK.stubFor(
                get(urlPathEqualTo("/xrpc/app.bsky.feed.getPosts"))
                        .withQueryParam("uris", equalTo("at://u1"))
                        .willReturn(
                                okJson(
                                        """
            {
                "posts": [
                    {
                        "uri": "at://u1",
                        "cid": "c1",
                        "author": {
                            "did": "did:plc:dww4ffboffsw3gk7ph4fizpc",
                            "handle": "metricbot.de",
                            "avatar": "https://cdn.bsky.app/img/avatar/plain/did:plc:dww4ffboffsw3gk7ph4fizpc/bafkreicjjtgtmayc2owzghovnsxqn7cdsrmuhbtypqyikkvwxbojntt4j4@jpeg",
                            "viewer": {
                                "muted": false,
                                "blockedBy": false,
                                "following": "at://did:plc:n5o2wksggcs653t3seg5eu6b/app.bsky.graph.follow/3kas6rdcc4f2k"
                            },
                            "labels": []
                        },
                        "record": {
                            "text": "Here you go: \\n\\nhttps://bsky.social/profile/metricbot.de/post/3kbbbkhvqbz2n\\n",
                            "$type": "app.bsky.feed.post",
                            "langs": [
                                "en"
                            ],
                            "reply": {
                                "root": {
                                    "cid": "bafyreihpnilfgj6et7dfq2ldfselawhokkaq3ns56pym7xjsyle3p2k7ru",
                                    "uri": "at://did:plc:n5o2wksggcs653t3seg5eu6b/app.bsky.feed.post/3kbbbk5fnkq2v"
                                },
                                "parent": {
                                    "cid": "bafyreihpnilfgj6et7dfq2ldfselawhokkaq3ns56pym7xjsyle3p2k7ru",
                                    "uri": "at://did:plc:n5o2wksggcs653t3seg5eu6b/app.bsky.feed.post/3kbbbk5fnkq2v"
                                }
                            },
                            "createdAt": "2023-10-08T19:57:07.686469251Z"
                        },
                        "replyCount": 0,
                        "repostCount": 0,
                        "likeCount": 0,
                        "indexedAt": "2023-10-08T19:57:07.686Z",
                        "viewer": {},
                        "labels": []
                    }
                ]
            }
            """)));

        // when:
        final var bskyStatus = client.sendReply(draft);

        // then:
        final var allServeEvents = WIREMOCK.getAllServeEvents().stream()
                .filter(se -> se.getRequest().getUrl().contains("createRecord"))
                .toList();

        assertThat(allServeEvents).isNotEmpty();

        final var serveEvent = allServeEvents.iterator().next();
        final var sentBody = serveEvent.getRequest().getBody();

        final var om = BskyJacksonProvider.INSTANCE.getObjectMapper();
        final var jsonNode = om.readTree(sentBody);

        // facets must be array node
        final var facets = jsonNode.get("record").get("facets");
        assertThat(facets).isInstanceOf(ArrayNode.class);

        // features must be array node
        final var firstFacetFeatures = facets.get(0).get("features");
        assertThat(firstFacetFeatures).isInstanceOf(ArrayNode.class);
    }
}
