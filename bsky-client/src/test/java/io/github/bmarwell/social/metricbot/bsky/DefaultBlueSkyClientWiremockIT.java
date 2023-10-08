package io.github.bmarwell.social.metricbot.bsky;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtProtoLoginResponse;
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
}
