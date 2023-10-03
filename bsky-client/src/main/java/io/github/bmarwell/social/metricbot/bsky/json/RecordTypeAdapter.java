package io.github.bmarwell.social.metricbot.bsky.json;

import io.github.bmarwell.social.metricbot.bsky.RecordType;
import jakarta.json.bind.adapter.JsonbAdapter;

public class RecordTypeAdapter implements JsonbAdapter<RecordType, String> {
    @Override
    public String adaptToJson(final RecordType obj) throws Exception {
        return obj.getTypeId();
    }

    @Override
    public RecordType adaptFromJson(final String obj) throws Exception {
        return RecordType.fromString(obj);
    }
}
