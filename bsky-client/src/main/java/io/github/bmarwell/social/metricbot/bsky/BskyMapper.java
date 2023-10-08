/*
 * Copyright 2023 The social-metricbot contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bmarwell.social.metricbot.bsky;

import io.github.bmarwell.social.metricbot.bsky.json.dto.*;
import io.github.bmarwell.social.metricbot.bsky.json.getposts.AtGetPostsPosts;

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
                atMentionNotification.record().reply().map(AtPostReply::parent).map(AtPostReplyInformation::uri),
                atMentionNotification.embed().map(AtEmbed::record).map(AtEmbedRecord::uri));
    }

    public static BskyStatus toStatus(final AtGetPostsPosts atGetPostsPosts) {
        final var record = atGetPostsPosts.record();
        return new BskyStatus(
                atGetPostsPosts.uri(),
                atGetPostsPosts.cid(),
                atGetPostsPosts.author(),
                record.text(),
                record.type(),
                record.lang(),
                record.createdAt(),
                record.reply().map(AtPostReply::parent).map(AtPostReplyInformation::uri),
                atGetPostsPosts.embed().map(AtEmbed::record).map(AtEmbedRecord::uri));
    }
}
