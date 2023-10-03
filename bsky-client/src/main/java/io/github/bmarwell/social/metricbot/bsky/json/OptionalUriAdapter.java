package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.adapter.JsonbAdapter;
import java.net.URI;
import java.util.Optional;

public class OptionalUriAdapter implements JsonbAdapter<Optional<URI>, String> {

    @SuppressWarnings("OptionalAssignedToNull")
    @Override
    public String adaptToJson(final Optional<URI> obj) throws Exception {
        if (obj == null || obj.isEmpty()) {
            return null;
        }

        return obj.map(URI::toString).orElseThrow();
    }

    @Override
    public Optional<URI> adaptFromJson(final String obj) throws Exception {
        if (obj == null) {
            return Optional.empty();
        }

        return Optional.of(URI.create(obj));
    }
}
