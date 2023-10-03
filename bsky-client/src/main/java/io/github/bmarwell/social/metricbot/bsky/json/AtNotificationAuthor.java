package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public record AtNotificationAuthor(
        @JsonbProperty("did") String did,
        @JsonbProperty("handle") String handle,
        @JsonbProperty("displayName") String displayName,
        @JsonbProperty("description") String description,
        @JsonbTypeAdapter(OptionalUriAdapter.class) @JsonbProperty("avatar") Optional<URI> avatarUri,
        @JsonbProperty("indexedAt") Instant indexedAt) {}
