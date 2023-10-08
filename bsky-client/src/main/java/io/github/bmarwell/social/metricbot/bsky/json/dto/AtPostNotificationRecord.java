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
package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.bmarwell.social.metricbot.bsky.RecordType;
import io.github.bmarwell.social.metricbot.bsky.json.RecordTypeAdapter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record AtPostNotificationRecord(
        @JsonProperty("text") String text,
        @JsonDeserialize(converter = RecordTypeAdapter.class) @JsonProperty("$type") RecordType type,
        @JsonProperty("lang") List<String> lang,
        @JsonProperty("createdAt") Instant createdAt,
        @JsonProperty("reply") Optional<AtPostReply> reply)
        implements AtNotificationRecord {

    public boolean isReply() {
        return this.reply().isPresent();
    }
}
