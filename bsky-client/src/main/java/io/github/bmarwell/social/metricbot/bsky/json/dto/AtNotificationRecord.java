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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.bmarwell.social.metricbot.bsky.RecordType;
import io.github.bmarwell.social.metricbot.bsky.json.RecordTypeAdapter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(name = "app.bsky.feed.post", value = AtPostNotificationRecord.class),
    @JsonSubTypes.Type(name = "app.bsky.graph.follow", value = AtNotificationFollowRecord.class),
    @JsonSubTypes.Type(name = "app.bsky.feed.like", value = AtNotificationLikeRecord.class),
    @JsonSubTypes.Type(name = "app.bsky.feed.repost", value = AtNotificationRepostRecord.class)
})
public sealed interface AtNotificationRecord
        permits AtNotificationFollowRecord,
                AtNotificationLikeRecord,
                AtPostNotificationRecord,
                AtNotificationRepostRecord {
    @JsonDeserialize(converter = RecordTypeAdapter.class)
    @JsonProperty("$type")
    RecordType type();
}
