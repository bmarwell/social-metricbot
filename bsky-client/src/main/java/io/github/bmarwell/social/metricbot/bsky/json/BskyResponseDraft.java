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
package io.github.bmarwell.social.metricbot.bsky.json;

import io.github.bmarwell.social.metricbot.bsky.BskyStatus;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtEmbedRecord;
import java.util.List;
import java.util.Optional;

public record BskyResponseDraft(
        String postStatus, BskyStatus postToReplyTo, List<AtLink> links, Optional<AtEmbedRecord> embedRecord) {

    public BskyResponseDraft(final String postStatus, final BskyStatus postToReplyTo) {
        this(postStatus, postToReplyTo, List.of(), Optional.empty());
    }
}
