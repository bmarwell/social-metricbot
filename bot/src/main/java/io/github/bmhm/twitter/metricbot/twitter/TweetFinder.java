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

import io.github.bmhm.twitter.metricbot.conversion.ImperialConversion;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@Prototype
public class TweetFinder {

  private static final Logger LOG = LoggerFactory.getLogger(TweetFinder.class);

  @Inject
  private Twitter twitter;

  /** recent replies. */
  @Inject
  @Named("recentMatches")
  private Map<Long, Instant> recentMatches;

  @Scheduled(
      initialDelay="${io.github.bmhm.twitter.metricbot.tweetfinder.initialdelay:5s}",
      fixedRate="${io.github.bmhm.twitter.metricbot.tweetfinder.rate:5s}"
  )
  public void findNewTweets() {
    LOG.info("Loading tweets.");
    Query query = new Query("lang:en \"degrees Fahrenheit\"");
    query.setLang("en");
    query.setResultType(ResultType.recent);
    query.setCount(50);

    try {
      final QueryResult queryResult = twitter.search(query);
      findMatching(queryResult);
    } catch (TwitterException twitterEx) {
      LOG.error("unable to retrieve tweets!", twitterEx);
    }
  }

  private void findNextTweets(QueryResult oldQueryResult) {
    if (!oldQueryResult.hasNext()) {
      return;
    }

    try {
      final QueryResult queryResult = twitter.search(oldQueryResult.nextQuery());
      findMatching(queryResult);
    } catch (TwitterException twitterEx) {
      LOG.error("unable to retrieve tweets!", twitterEx);
    }
  }

  private void findMatching( QueryResult queryResult) {
    LOG.info("Searching in [{}] tweets", queryResult.getCount());
    final Optional<Status> fahrenheitTweet = queryResult.getTweets().stream()
        .filter(tweet -> !recentMatches.containsKey(tweet.getId()))
        .filter(tweet -> ImperialConversion.degreesFahrenheit.matcher(tweet.getText()).find())
        .findAny();

    fahrenheitTweet.ifPresentOrElse(this::reply, () -> findNextTweets(queryResult));
  }

  private void reply(Status status) {
    LOG.info("Matcher for Status: [{}].", status);
    recentMatches.put(status.getId(), status.getCreatedAt().toInstant());
    if (recentMatches.size() > 199) {
      Optional<Entry<Long, Instant>> any = recentMatches.entrySet().stream()
          .min((one, other) -> (int) (other.getValue().getEpochSecond() - one.getValue().getEpochSecond()));
      LOG.info("Remove [{}].", status.getId());
      any.ifPresent(entry -> recentMatches.remove(entry.getKey()));
    }

    LOG.trace("Map: [{}].", recentMatches);

    final String converted = new ImperialConversion().returnConverted(status.getText());
    LOG.info("For your convenience, here are the metric units:\n{}.", converted);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TweetFinder.class.getSimpleName() + "[", "]")
        .add("twitter=" + twitter)
        .toString();
  }
}
