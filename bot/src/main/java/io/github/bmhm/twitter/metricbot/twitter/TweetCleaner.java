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

package io.github.bmhm.twitter.metricbot.twitter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.ZonedDateTime;

import io.github.bmhm.twitter.metricbot.db.dao.TweetRepository;
import io.micronaut.scheduling.annotation.Scheduled;

@Singleton
public class TweetCleaner {

  /**
   * recent replies.
   */
  @Inject
  private TweetRepository tweetRepository;

  public TweetCleaner() {
    // injection
  }

  @Scheduled(initialDelay = "5s", fixedDelay = "10m")
  public void removeOldTweets() {
    final ZonedDateTime now = ZonedDateTime.now();
    final ZonedDateTime oneWeekAgo = now.minusDays(7L);
    final Instant deleteBefore = oneWeekAgo.toInstant();

    this.tweetRepository.deleteByTweetTimeBefore(deleteBefore);
  }

}
