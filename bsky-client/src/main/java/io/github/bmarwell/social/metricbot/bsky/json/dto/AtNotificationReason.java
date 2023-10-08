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

import java.util.Arrays;

public enum AtNotificationReason {
    FOLLOW("follow"),
    REPOST("repost"),
    LIKE("like"),
    MENTION("mention"),
    UNKNOWN("unknown");

    private final String reasonString;

    AtNotificationReason(final String reasonString) {
        this.reasonString = reasonString;
    }

    public String getReasonString() {
        return reasonString;
    }

    public static AtNotificationReason fromString(final String reasonString) {
        return Arrays.stream(AtNotificationReason.values())
                .filter(anr -> anr.getReasonString().equals(reasonString))
                .findFirst()
                .orElse(AtNotificationReason.UNKNOWN);
    }
}
