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

import io.micronaut.context.annotation.Prototype;
import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class TweetFinder {

  private static final Logger LOG = LoggerFactory.getLogger(TweetFinder.class);

  @Scheduled(
      initialDelay="${io.github.bmhm.twitter.metricbot.tweetfinder.initialdelay:5s}",
      fixedRate="${io.github.bmhm.twitter.metricbot.tweetfinder.rate:1m}"
  )
  public void findNewTweets() {
    LOG.info("Loading tweets.");
  }

}
