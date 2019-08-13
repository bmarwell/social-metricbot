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

import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "SCANNED_TWEETS")
@Cacheable
public class TweetPdo {

  public static final long ID_NOT_SET = -1L;

  @Id
  @Column(name = "TWEET_ID", columnDefinition = "BIGINT")
  private long tweedId = ID_NOT_SET;

  @Basic
  @Column(name = "RESPONSE_ID", columnDefinition = "BIGINT", nullable = false)
  private long botResponseId = ID_NOT_SET;

  public TweetPdo() {
    // jpa requirement
  }

  public TweetPdo(final long tweedId, final long botResponseId) {
    this.tweedId = tweedId;
    this.botResponseId = botResponseId;
  }

  @PrePersist
  public void check() {
    if (this.tweedId == ID_NOT_SET) {
      throw new IllegalStateException("id not set");
    }
  }

  public long getTweedId() {
    return this.tweedId;
  }

  public void setTweedId(final long tweedId) {
    this.tweedId = tweedId;
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

  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    final TweetPdo tweetPdo = (TweetPdo) other;
    return this.tweedId == tweetPdo.tweedId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.tweedId);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TweetPdo.class.getSimpleName() + "[", "]")
        .add("tweedId=" + this.tweedId)
        .add("botResponseId=" + this.botResponseId)
        .toString();
  }
}
