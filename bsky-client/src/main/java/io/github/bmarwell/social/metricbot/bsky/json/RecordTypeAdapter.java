package io.github.bmarwell.social.metricbot.bsky.json;

import com.fasterxml.jackson.databind.util.StdConverter;
import io.github.bmarwell.social.metricbot.bsky.RecordType;

public class RecordTypeAdapter extends StdConverter<String, RecordType> {

    @Override
    public RecordType convert(final String value) {
        return RecordType.fromString(value);
    }
}
