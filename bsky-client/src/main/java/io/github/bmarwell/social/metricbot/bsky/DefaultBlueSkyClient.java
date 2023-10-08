package io.github.bmarwell.social.metricbot.bsky;

import io.github.bmarwell.social.metricbot.bsky.json.BskyResponseDraft;
import io.github.bmarwell.social.metricbot.bsky.json.JsonReader;
import io.github.bmarwell.social.metricbot.bsky.json.JsonWriter;
import io.github.bmarwell.social.metricbot.bsky.json.dto.*;
import io.github.bmarwell.social.metricbot.bsky.json.getposts.AtGetPostsResponseWrapper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import java.io.Serial;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBlueSkyClient implements BlueSkyClient {

    @Serial
    private static final long serialVersionUID = -6379484000282531656L;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultBlueSkyClient.class);
    private final Client client;
    private final MutableBlueSkyConfiguration bskyConfig;
    /**
     * JSON web token returned from successful login.
     */
    private final AtomicReference<String> accessToken = new AtomicReference<>();

    private Instant refreshBefore = Instant.MAX;
    private boolean loginNotSuccessfull;
    private boolean isLoggedIn;

    public DefaultBlueSkyClient(final MutableBlueSkyConfiguration bsc) {
        this.bskyConfig = bsc.clone();
        this.client = ClientBuilder.newClient().register(new JsonReader<>()).register(new JsonWriter<>())
        // end
        ;
    }

    private CompletableFuture<Void> ensureLoggedIn() {
        if (isLoggedIn) {
            LOG.debug("[BSKY] Already logged in.");

            if (Instant.now().isAfter(this.refreshBefore.minusSeconds(60))) {
                return doLoginAndSetVariables();
            }

            return CompletableFuture.completedFuture(null);
        }

        if (loginNotSuccessfull) {
            LOG.warn("[BSKY] Previous login not successful. Don't attempt again.");
            return CompletableFuture.failedFuture(new IllegalStateException("Already tried to log in."));
        }

        return doLoginAndSetVariables();
    }

    private CompletableFuture<Void> doLoginAndSetVariables() {
        return CompletableFuture.supplyAsync(this::doLogin)
                .handle((final Optional<AtProtoLoginResponse> result, final Throwable error) -> {
                    if (error != null) {
                        LOG.error("[BSKY] Login not successful.", error);

                        this.accessToken.set("");
                        this.isLoggedIn = false;
                        this.loginNotSuccessfull = true;
                        return null;
                    }

                    LOG.info("[BSKY] Login was successful.");

                    this.accessToken.set(result.orElseThrow().accessJwt());
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

    @Override
    public Optional<BskyStatus> getRepliedToPost(final BskyStatus status) {
        if (!status.isReply()) {
            return Optional.empty();
        }

        final var replyTo = status.inReplyTo().orElseThrow();

        return ensureLoggedIn()
                .thenApply((final Void __) -> getSinglePost(replyTo))
                .join();
    }

    @Override
    public Optional<BskyStatus> getSinglePost(final URI replyTo) {
        try (final var response = this.client
                .target("https://bsky.social/xrpc/app.bsky.feed.getPosts")
                .queryParam("uris", replyTo.toASCIIString())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + this.accessToken.get())
                .get()) {

            if (response.getStatus() != 200) {
                final String entity;
                if (response.hasEntity()) {
                    entity = response.readEntity(String.class);
                } else {
                    entity = "<empty>";
                }

                LOG.error(
                        "Unable to get post [{}]. Status = [{}]. Response body: [{}].",
                        replyTo,
                        response.getStatus(),
                        entity);
                return Optional.empty();
            }

            if (!response.hasEntity()) {
                LOG.error("Unable to get post [{}] body: no entity. Status = [{}]", replyTo, response.getStatus());
                return Optional.empty();
            }

            final var responseEntity = response.readEntity(AtGetPostsResponseWrapper.class);
            if (responseEntity.posts().isEmpty()) {
                LOG.error("Empty posts response for URI [{}]: [{}].", replyTo, responseEntity);
                return Optional.empty();
            }

            final var atGetPostsPosts = responseEntity.posts().get(0);
            final var atGetPostsStatus = BskyMapper.toStatus(atGetPostsPosts);

            return Optional.of(atGetPostsStatus);
        } catch (RuntimeException rtEx) {
            LOG.error("Problem mapping the entity. Entity.", rtEx);

            return Optional.empty();
        }
    }

    @Override
    public boolean isByOtherUser(final BskyStatus status) {
        return !this.bskyConfig.getHandle().equals(status.author().handle());
    }

    @Override
    public Optional<BskyStatus> getRepostedStatus(final BskyStatus status) {
        return ensureLoggedIn()
                .thenApply(
                        (final Void __) -> getSinglePost(status.quotedStatus().orElseThrow()))
                .join();
    }

    @Override
    public Optional<BskyStatus> sendReply(final BskyResponseDraft statusDraft) {
        return ensureLoggedIn()
                .thenApply((final Void __) -> doSendReply(statusDraft))
                .join();
    }

    @Override
    public String getHandle() {
        return this.bskyConfig.getHandle();
    }

    @Override
    public URI getStatusUri(final BskyStatus bskyStatus) {
        final var uri = bskyStatus.uri();
        final var postId = Paths.get(uri.getPath()).getFileName().toString();

        return UriBuilder.fromUri("https://bsky.app/")
                .path("profile")
                .path(bskyStatus.author().handle())
                .path("post")
                .path(postId)
                .build();
    }

    private Optional<BskyStatus> doSendReply(final BskyResponseDraft statusDraft) {
        final var reply = Map.of(
                "root",
                Map.of(
                        "uri", statusDraft.postToReplyTo().uri().toString(),
                        "cid", statusDraft.postToReplyTo().cid()
                        // end of root
                        ),
                "parent",
                Map.of(
                        "uri", statusDraft.postToReplyTo().uri().toString(),
                        "cid", statusDraft.postToReplyTo().cid()
                        // end of parent
                        )
                // end of reply
                );
        final var record = Map.of(
                "text", statusDraft.postStatus(),
                "createdAt", Instant.now().toString(),
                "$type", "app.bsky.feed.post",
                "langs", List.of("en"),
                "reply", reply
                // end of map
                );
        final var postEntity =
                Map.of("collection", "app.bsky.feed.post", "repo", this.bskyConfig.getHandle(), "record", record);
        final var content = Entity.json(postEntity);
        try (final var response = this.client
                .target("https://bsky.social/xrpc/com.atproto.repo.createRecord")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + this.accessToken)
                .post(content)) {
            if (response.getStatus() != 200) {
                final String responseEntity;
                if (response.hasEntity()) {
                    responseEntity = response.readEntity(String.class);
                } else {
                    responseEntity = "";
                }
                LOG.error("Response != 200: [{}].", responseEntity);
            } else {
                final var atEmbedRecord = response.readEntity(AtEmbedRecord.class);
                LOG.debug("Response sent successfully! [{}]", atEmbedRecord);
                return this.getSinglePost(atEmbedRecord.uri());
            }
        } catch (final RuntimeException rtEx) {
            LOG.error("Problem sending data.", rtEx);
        }

        return Optional.empty();
    }

    private List<BskyStatus> doGetRecentMentions() {
        try (final var response = this.client
                .target("https://bsky.social/xrpc/app.bsky.notification.listNotifications")
                .queryParam("limit", "15")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + this.accessToken.get())
                .get()) {
            if (response.getStatus() == 200 && response.hasEntity()) {
                final var atNotificationResponse = response.readEntity(AtNotificationResponseWrapper.class);

                if (LOG.isTraceEnabled()) {
                    LOG.trace("[BSKY] got notifications: >>" + atNotificationResponse + "<<");
                }

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
        this.accessToken.set("");
        this.refreshBefore = Instant.MAX;
        this.loginNotSuccessfull = true;
        this.isLoggedIn = false;
        this.client.close();
    }
}
