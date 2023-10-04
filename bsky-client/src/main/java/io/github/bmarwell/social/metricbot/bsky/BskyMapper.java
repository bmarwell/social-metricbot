package io.github.bmarwell.social.metricbot.bsky;

import io.github.bmarwell.social.metricbot.bsky.json.AtMentionNotification;
import io.github.bmarwell.social.metricbot.bsky.json.AtPostReply;
import io.github.bmarwell.social.metricbot.bsky.json.AtPostReplyInformation;

public final class BskyMapper {
    private BskyMapper() {
        // util
    }

    public static BskyStatus toStatus(final AtMentionNotification atMentionNotification) {
        return new BskyStatus(
                atMentionNotification.uri(),
                atMentionNotification.cid(),
                atMentionNotification.author(),
                atMentionNotification.record().text(),
                atMentionNotification.record().type(),
                atMentionNotification.record().lang(),
            atMentionNotification.record().createdAt(),
            atMentionNotification.record().reply()
                .map(AtPostReply::parent)
                .map(AtPostReplyInformation::cid)
        );
    }
}
