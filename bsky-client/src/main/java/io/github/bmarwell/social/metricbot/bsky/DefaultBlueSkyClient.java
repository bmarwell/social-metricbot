package io.github.bmarwell.social.metricbot.bsky;

import io.github.bmarwell.social.metricbot.bsky.json.AtProtoLoginBody;
import io.github.bmarwell.social.metricbot.bsky.json.AtProtoLoginResponse;
import io.github.bmarwell.social.metricbot.bsky.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serial;
import java.time.Instant;
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

        return CompletableFuture.supplyAsync(this::doLogin)
                .handle((final Optional<AtProtoLoginResponse> result, final Throwable error) -> {
                    if (error != null) {
                        this.accessToken = "";
                        this.refreshToken = "";
                        this.isLoggedIn = false;
                        this.loginNotSuccessfull = true;
                        return null;
                    }

                    this.accessToken = result.orElseThrow().accessJwt();
                    this.refreshToken = result.orElseThrow().refreshJwt();
                    this.isLoggedIn = true;
                    this.loginNotSuccessfull = false;
                    return null;
                });
    }

    private Optional<AtProtoLoginResponse> doLogin() {
        final AtProtoLoginBody atProtoLoginBody =
                new AtProtoLoginBody(this.bskyConfig.getHandle(), this.bskyConfig.getAppSecret());
        final Response response = this.client
                .target("https://bsky.social/xrpc/com.atproto.server.createSession")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(atProtoLoginBody, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() == 200 && response.hasEntity()) {
            final AtProtoLoginResponse atProtoLoginResponse = response.readEntity(AtProtoLoginResponse.class);

            return Optional.of(atProtoLoginResponse);
        }

        return Optional.empty();
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
