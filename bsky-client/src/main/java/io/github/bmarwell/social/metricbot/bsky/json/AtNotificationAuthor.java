package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public record AtNotificationAuthor(
        @JsonProperty("did") String did,
        @JsonProperty("handle") String handle,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("description") String description,
        @JsonProperty("avatar") Optional<URI> avatarUri,
        @JsonProperty("indexedAt") Instant indexedAt) {}
