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

import java.util.Arrays;

public enum RecordType {
    POST("app.bsky.feed.post"),
    LIKE("app.bsky.feed.like"),
    REPOST("app.bsky.feed.repost"),
    FOLLOW("app.bsky.graph.follow"),
    UNKNOWN("unknown");

    private final String typeId;

    RecordType(final String typeId) {
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }

    public static RecordType fromString(final String typeId) {
        return Arrays.stream(RecordType.values())
                .filter(pt -> pt.getTypeId().equals(typeId))
                .findFirst()
                .orElse(RecordType.UNKNOWN);
    }
}
