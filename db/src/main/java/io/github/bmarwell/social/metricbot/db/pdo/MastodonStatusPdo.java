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
package io.github.bmarwell.social.metricbot.db.pdo;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Optional;

@Entity
@Table(name = "MASTODON_STATUS")
public class MastodonStatusPdo {

    public static final Instant TIME_NOT_SET = Instant.EPOCH;

    @Id
    @Column(name = "STATUS_ID")
    private String id;

    @Column(name = "TWEET_TIME", nullable = false)
    private Instant tweetTime;

    @Column(name = "bot_response_id", nullable = false)
    private String botResponseId;

    @Column(name = "RESPONSE_TIME", nullable = false)
    private Instant responseTime;

    public MastodonStatusPdo() {
        this.botResponseId = "";
        this.responseTime = TIME_NOT_SET;
    }

    public MastodonStatusPdo(String id, Instant tweetTime) {
        this.id = id;
        this.tweetTime = tweetTime;
        this.botResponseId = "";
        this.responseTime = TIME_NOT_SET;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTweetTime() {
        return tweetTime;
    }

    public void setTweetTime(Instant tweetTime) {
        this.tweetTime = tweetTime;
    }

    public void setBotResponseId(final @Nullable String botResponseId) {
        this.botResponseId = Optional.ofNullable(botResponseId).orElse("");
    }

    public Instant getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Instant responseTime) {
        this.responseTime = responseTime;
    }

    public @Nullable String getBotResponseId() {
        return this.botResponseId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MastodonStatusPdo that)) {
            return false;
        }

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
