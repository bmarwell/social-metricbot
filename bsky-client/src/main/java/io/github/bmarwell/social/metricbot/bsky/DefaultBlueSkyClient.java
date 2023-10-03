package io.github.bmarwell.social.metricbot.bsky;

import io.github.bmarwell.social.metricbot.bsky.json.*;
import jakarta.json.bind.Jsonb;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.io.Serial;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBlueSkyClient implements BlueSkyClient {

    @Serial
    private static final long serialVersionUID = -6379484000282531656L;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultBlueSkyClient.class);
    private final Jsonb jsonb;
    private final Client client;
    private final MutableBlueSkyConfiguration bskyConfig;
    /**
     * JSON web token returned from successful login.
     */
    private String accessToken;

    private String refreshToken;
    private Instant refreshBefore = Instant.MAX;
    private boolean loginNotSuccessfull;
    private boolean isLoggedIn;

    public DefaultBlueSkyClient(final MutableBlueSkyConfiguration bsc) {
        this.bskyConfig = bsc.clone();
        this.jsonb = BskyJsonbProvider.INSTANCE.getJsonb();
        this.client =
                ClientBuilder.newClient().register(new JsonReader<>(this.jsonb)).register(new JsonWriter<>(this.jsonb))
        // end
        ;
    }

    private CompletableFuture<Void> ensureLoggedIn() {
        if (isLoggedIn) {
            LOG.info("[BSKY] Already logged in.");
            return CompletableFuture.completedFuture(null);
        }

        if (loginNotSuccessfull) {
            LOG.warn("[BSKY] Previous login not successful. Don't attempt again.");
            return CompletableFuture.failedFuture(new IllegalStateException("Already tried to log in."));
        }

        return CompletableFuture.supplyAsync(this::doLogin)
                .handle((final Optional<AtProtoLoginResponse> result, final Throwable error) -> {
                    if (error != null) {
                        LOG.error("[BSKY] Login not successful.", error);

                        this.accessToken = "";
                        this.refreshToken = "";
                        this.isLoggedIn = false;
                        this.loginNotSuccessfull = true;
                        return null;
                    }

                    LOG.info("[BSKY] Login was successful.");

                    this.accessToken = result.orElseThrow().accessJwt();
                    this.refreshToken = result.orElseThrow().refreshJwt();
                    this.isLoggedIn = true;
                    this.loginNotSuccessfull = false;
                    return null;
                });
    }

    private Optional<AtProtoLoginResponse> doLogin() {
        final AtProtoLoginBody atProtoLoginBody =
                AtProtoLoginBody.from(this.bskyConfig.getHandle(), this.bskyConfig.getAppSecret());
        try (final var response = this.client
                .target("https://bsky.social/xrpc/com.atproto.server.createSession")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(atProtoLoginBody, MediaType.APPLICATION_JSON_TYPE))) {
            if (response.getStatus() == 200 && response.hasEntity()) {

                final AtProtoLoginResponse atProtoLoginResponse = response.readEntity(AtProtoLoginResponse.class);

                return Optional.of(atProtoLoginResponse);
            } else {
                final String responseBody;
                if (response.hasEntity()) {
                    responseBody = response.readEntity(String.class);
                } else {
                    responseBody = "empty";
                }
                throw new IllegalStateException(
                        "Login not successful. RC=" + response.getStatus() + ". Body: >>" + responseBody + "<<.");
            }
        }
    }

    @Override
    public CompletableFuture<List<BskyStatus>> getRecentMentions() {
        return ensureLoggedIn().thenApply((final Void __) -> doGetRecentMentions());
    }

    private List<BskyStatus> doGetRecentMentions() {
        try (final var response = this.client
                .target("https://bsky.social/xrpc/app.bsky.notification.listNotifications")
                .queryParam("limit", "15")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + this.accessToken)
                .get()) {
            if (response.getStatus() == 200 && response.hasEntity()) {
                final var atNotificationResponse = response.readEntity(AtNotificationResponseWrapper.class);

                LOG.info("[BSKY] got notifications: >>" + atNotificationResponse + "<<");

                return atNotificationResponse.notifications().stream()
                        .filter(atn -> atn.reason() == AtNotificationReason.MENTION)
                        .filter(atn -> atn instanceof AtMentionNotification)
                        .map(atn -> (AtMentionNotification) atn)
                        .filter(atn -> atn.record().type() == RecordType.POST)
                        .map(BskyMapper::toStatus)
                        .toList();
            } else {
                final String responseBody;
                if (response.hasEntity()) {
                    responseBody = response.readEntity(String.class);
                } else {
                    responseBody = "empty";
                }
                throw new IllegalStateException("Getting notifications not successful. RC=" + response.getStatus()
                        + ". Body: >>" + responseBody + "<<.");
            }
        }
    }

    public Client getClient() {
        return client;
    }

    @Override
    public void close() throws Exception {
        this.accessToken = "";
        this.refreshToken = "";
        this.refreshBefore = Instant.MAX;
        this.loginNotSuccessfull = true;
        this.isLoggedIn = false;
        this.client.close();
        this.jsonb.close();
    }
}
