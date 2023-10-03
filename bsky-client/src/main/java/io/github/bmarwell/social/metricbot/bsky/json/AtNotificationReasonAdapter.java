package io.github.bmarwell.social.metricbot.bsky.json;

import jakarta.json.bind.adapter.JsonbAdapter;

public class AtNotificationReasonAdapter implements JsonbAdapter<AtNotificationReason, String> {
    @Override
    public String adaptToJson(final AtNotificationReason obj) throws Exception {
        return obj.getReasonString();
    }

    @Override
    public AtNotificationReason adaptFromJson(final String obj) throws Exception {
        return AtNotificationReason.fromString(obj);
    }
}
