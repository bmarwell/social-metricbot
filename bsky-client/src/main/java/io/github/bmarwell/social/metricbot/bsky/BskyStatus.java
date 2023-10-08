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

import io.github.bmarwell.social.metricbot.bsky.json.dto.AtNotificationAuthor;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record BskyStatus(
        URI uri,
        String cid,
        AtNotificationAuthor author,
        String text,
        RecordType type,
        List<String> lang,
        Instant createdAt,
        Optional<URI> inReplyTo,
        Optional<URI> quotedStatus) {

    public boolean isReply() {
        return inReplyTo().isPresent();
    }

    /**
     * Returns {@code true} if this status quotes (embeds) another post.
     * <p>
     * This is equivalent to {@code quotedStatus().isPresent()}.
     * </p>
     *
     * @return {@code true} if this status quotes (embeds) another post.
     */
    public boolean isQuoting() {
        return quotedStatus().isPresent();
    }
}
