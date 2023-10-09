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

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "reason")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AtMentionNotification.class, name = "mention"),
    @JsonSubTypes.Type(value = AtFollowNotification.class, name = "follow"),
    @JsonSubTypes.Type(value = AtLikeNotification.class, name = "like"),
    @JsonSubTypes.Type(value = AtRepostNotification.class, name = "repost"),
    @JsonSubTypes.Type(value = AtQuoteNotification.class, name = "quote"),
    @JsonSubTypes.Type(value = AtReplyNotification.class, name = "reply"),
})
public sealed interface AtNotification
        permits AtFollowNotification,
                AtLikeNotification,
                AtMentionNotification,
                AtQuoteNotification,
                AtReplyNotification,
                AtRepostNotification {
    @JsonDeserialize(converter = AtNotificationReasonAdapter.class)
    @JsonProperty("reason")
    AtNotificationReason reason();
}
