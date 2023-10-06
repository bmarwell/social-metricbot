package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.databind.util.StdConverter;

public class AtNotificationReasonAdapter extends StdConverter<String, AtNotificationReason> {

    @Override
    public AtNotificationReason convert(final String value) {
        return AtNotificationReason.fromString(value);
    }
}
