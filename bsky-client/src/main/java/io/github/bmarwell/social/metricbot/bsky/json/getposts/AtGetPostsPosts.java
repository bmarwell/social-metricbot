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
package io.github.bmarwell.social.metricbot.bsky.json.getposts;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtEmbed;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtNotificationAuthor;
import io.github.bmarwell.social.metricbot.bsky.json.dto.AtPostNotificationRecord;
import java.net.URI;
import java.util.Optional;

public record AtGetPostsPosts(
        @JsonProperty("uri") URI uri,
        @JsonProperty("cid") String cid,
        @JsonProperty("author") AtNotificationAuthor author,
        @JsonProperty("record") AtPostNotificationRecord record,
        @JsonProperty("embed") Optional<AtEmbed> embed) {}
