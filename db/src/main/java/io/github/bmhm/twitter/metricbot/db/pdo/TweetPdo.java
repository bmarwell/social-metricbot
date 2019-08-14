/*
 *  Copyright 2018 The twittermetricbot contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.bmhm.twitter.metricbot.db.pdo;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.time.Instant;
import java.util.Objects;
import java.util.StringJoiner;

@MappedEntity("Tweet")
public class TweetPdo {

  public static final long ID_NOT_SET = -1L;

  @Id
  private long tweetId = ID_NOT_SET;

  private Instant tweetTime;

  private long botResponseId = ID_NOT_SET;

  private @Nullable Instant responseTime;

  public TweetPdo() {
    // jpa requirement
  }

  public TweetPdo(final long tweetId, final Instant tweetTime, final long botResponseId) {
    this.tweetId = tweetId;
    this.botResponseId = botResponseId;
    this.tweetTime = tweetTime;
  }

  public TweetPdo(final long id, final Instant tweetTime) {
    this.tweetId = id;
    this.tweetTime = tweetTime;
  }

  public long getTweetId() {
    return this.tweetId;
  }

  public void setTweetId(final long tweetId) {
    this.tweetId = tweetId;
  }

  public Instant getTweetTime() {
    return this.tweetTime;
  }

  public void setTweetTime(final Instant tweetTime) {
    this.tweetTime = tweetTime;
  }

  public long getBotResponseId() {
    return this.botResponseId;
  }

  public void setBotResponseId(final long botResponseId) {
    if (botResponseId <= 0) {
      this.botResponseId = ID_NOT_SET;
      return;
    }

    this.botResponseId = botResponseId;
  }

  public Instant getResponseTime() {
    return this.responseTime;
  }

  public void setResponseTime(final Instant responseTime) {
    this.responseTime = responseTime;
  }

  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    final TweetPdo tweetPdo = (TweetPdo) other;
    return this.tweetId == tweetPdo.tweetId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.tweetId);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TweetPdo.class.getSimpleName() + "[", "]")
        .add("tweetId=" + this.tweetId)
        .add("tweetTime=" + this.tweetTime)
        .add("botResponseId=" + this.botResponseId)
        .add("responseTime=" + this.responseTime)
        .toString();
  }
}
