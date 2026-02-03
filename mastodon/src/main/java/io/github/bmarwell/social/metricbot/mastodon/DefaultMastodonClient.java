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
package io.github.bmarwell.social.metricbot.mastodon;

import io.github.bmarwell.social.metricbot.mastodon.xml.ParserUtil;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMastodonClient implements MastodonClient {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ParserUtil parserUtil = new ParserUtil();
    private final MastodonConfigurationBuilder mastodonConfig;

    private final Jsonb jsonb;

    private final Client client;
    boolean isLoggedIn = false;
    boolean loginNotSuccessfull = false;
    private String token;

    public DefaultMastodonClient(MastodonConfigurationBuilder mcb) {
        this.mastodonConfig = mcb;
        this.jsonb = JsonbBuilder.create();
        this.client = ClientBuilder.newClient().register(new JsonReader<>(this.jsonb));
    }

    private CompletableFuture<Void> ensureLoggedIn() {
        if (isLoggedIn) {
            return CompletableFuture.completedFuture(null);
        }

        if (loginNotSuccessfull) {
            return CompletableFuture.failedFuture(new IllegalStateException("Already tried to log in."));
        }

        this.isLoggedIn = true;
        this.token = mastodonConfig.getAccessToken();
        this.loginNotSuccessfull = false;

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<List<MastodonStatus>> getRecentMentions() {
        return ensureLoggedIn().thenApply((__) -> doGetRecentMentions());
    }

    @Override
    public CompletableFuture<Optional<MastodonStatus>> getStatusById(MastodonStatusId id) {
        return ensureLoggedIn().thenApply((__) -> doGetStatusById(id));
    }

    @Override
    public CompletableFuture<Optional<MastodonStatus>> postStatus(MastodonTextStatusDraft statusToSend) {
        return ensureLoggedIn().thenApply((__) -> doPostStatusReply(statusToSend));
    }

    private Optional<MastodonStatus> doPostStatusReply(MastodonTextStatusDraft statusToSend) {
        Map<String, String> statusFormMap = Map.of(
                "status", statusToSend.tootText(),
                "in_reply_to_id", statusToSend.replyToId().value(),
                "visibility", statusToSend.visibility().getValue(),
                "language", statusToSend.language().getLanguageCode());
        Entity<?> postContent = Entity.form(new MultivaluedHashMap<>(statusFormMap));
        try (Response response = this.client
                .target(mastodonConfig.getInstanceHost())
                .path("/api/v1/statuses")
                .request()
                .header("Authorization", "Bearer " + this.token)
                .post(postContent)) {
            if (response.getStatus() != 200) {
                log.error("RC of post reply: [{}]", response.getStatus());

                if (response.hasEntity()) {
                    log.error("post response: [{}]", response.readEntity(String.class));
                }

                return Optional.empty();
            }

            Map<String, Object> statusPostResponse = response.readEntity(Map.class);

            return Optional.of(statusMapToStatus(statusPostResponse));
        }
    }

    private Optional<MastodonStatus> doGetStatusById(MastodonStatusId id) {
        Response response = this.client
                .target(mastodonConfig.getInstanceHost())
                .path("/api/v1/statuses/" + id.value())
                .request()
                .header("Authorization", "Bearer " + this.token)
                .get();

        if (response.getStatus() == 200 && response.hasEntity()) {
            Map<String, Object> map = response.readEntity(Map.class);

            return Optional.ofNullable(statusMapToStatus(map));
        }

        return Optional.empty();
    }

    private List<MastodonStatus> doGetRecentMentions() {
        Response response = this.client
                .target(mastodonConfig.getInstanceHost())
                .path("/api/v1/notifications")
                .queryParam("limit", 20)
                .queryParam("types", "mention")
                .request()
                .header("Authorization", "Bearer " + this.token)
                .get();

        if (response.getStatus() != 200 || !response.hasEntity()) {
            String entity = null;

            if (response.hasEntity()) {
                entity = response.readEntity(String.class);
            }

            throw new IllegalStateException("Not found, RC=" + response.getStatus() + "; entity = " + entity);
        }

        //noinspection unchecked
        List<Map<String, Object>> notifications = (List<Map<String, Object>>) response.readEntity(List.class);

        return notifications.stream()
                .filter(nf -> nf.containsKey("status"))
                .map(this::notificationToToot)
                .toList();
    }

    private MastodonStatus notificationToToot(Map<String, Object> notificationEntry) {
        Map<String, Object> status = (Map<String, Object>) notificationEntry.get("status");

        return statusMapToStatus(status);
    }

    private MastodonStatus statusMapToStatus(Map<String, Object> status) {
        var id = new MastodonStatusId((String) status.get("id"));
        Optional<MastodonStatusId> inReplyToId =
                Optional.ofNullable((String) status.get("in_reply_to_id")).map(MastodonStatusId::new);
        var content = (String) status.get("content");
        var rawContent = parserUtil.getRawText(content);
        var uri = Optional.of((String) status.get("uri")).map(URI::create).orElseThrow();
        var url = Optional.of((String) status.get("url")).map(URI::create).orElseThrow();
        var createdAt = Optional.of((String) status.get("created_at"))
                .map(Instant::parse)
                .orElseThrow();
        //noinspection unchecked
        var mentions = ((List<Map<String, Object>>) status.get("mentions"))
                .stream().map(this::toMention).toList();

        long favouritesCount = getFavouritesCount(status);

        var account = getAccount(status);
        var isReblogged = false; // (Boolean) status.get("reblogged");
        var reblogged = Optional.<MastodonStatus>empty();

        return new MastodonStatus(
                id,
                inReplyToId,
                favouritesCount,
                content,
                rawContent,
                uri,
                url,
                createdAt,
                mentions,
                account,
                isReblogged,
                reblogged);
    }

    private MastodonAccount getAccount(Map<String, Object> status) {
        //noinspection unchecked
        Map<String, Object> account = (Map<String, Object>) status.get("account");
        return new MastodonAccount(
                new MastodonAccountId((String) account.get("id")),
                (String) account.get("acct"),
                (String) account.get("username"),
                (Boolean) account.get("locked"));
    }

    private long getFavouritesCount(Map<String, Object> status) {
        Object favouritesCount = status.get("favourites_count");
        if (favouritesCount instanceof Integer favInt) {
            return favInt;
        } else if (favouritesCount instanceof Long favLong) {
            return favLong;
        } else if (favouritesCount instanceof BigDecimal favDec) {
            return favDec.longValueExact();
        } else {
            throw new IllegalStateException("Cannot parse number from: " + favouritesCount);
        }
    }

    private MastodonMention toMention(Map<String, Object> mentionEntry) {
        var accountId = new MastodonAccountId((String) mentionEntry.get("id"));
        var url = Optional.of((String) mentionEntry.get("url")).map(URI::create).orElseThrow();

        return new MastodonMention(
                accountId, url, (String) mentionEntry.get("acct"), (String) mentionEntry.get("username"));
    }

    @Override
    public void close() throws Exception {
        this.token = "";
        this.loginNotSuccessfull = true;
        this.isLoggedIn = false;
        this.client.close();
        this.jsonb.close();
    }
}
