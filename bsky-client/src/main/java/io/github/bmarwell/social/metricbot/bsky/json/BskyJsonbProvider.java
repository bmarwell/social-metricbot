package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

public enum BskyJsonbProvider {
    INSTANCE;

    private final Jsonb jsonb;

    BskyJsonbProvider() {
        final var cfg = new JsonbConfig().withAdapters(new RecordTypeAdapter(), new AtNotificationReasonAdapter());
        this.jsonb = JsonbBuilder.newBuilder().withConfig(cfg).build();
    }

    public Jsonb getJsonb() {
        return jsonb;
    }
}
