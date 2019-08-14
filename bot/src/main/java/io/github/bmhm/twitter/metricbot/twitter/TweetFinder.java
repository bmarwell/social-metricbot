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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import io.github.bmhm.twitter.metricbot.conversion.UsConversion;
import io.github.bmhm.twitter.metricbot.db.dao.TweetRepository;
import io.github.bmhm.twitter.metricbot.db.pdo.TweetPdo;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  @Inject
  private UsConversion converter;

  /** recent replies. */
  @Inject
  private TweetRepository tweetRepository;

  private static final List<String> ACCOUNT_NAME_WORD_BLACKLIST = asList(
      "Boutique", "Crazy I Buy", "weather", "Supplements", "DealsSupply");

  public TweetFinder() {
    // injection constructor
  }

  @Scheduled(
      initialDelay="${io.github.bmhm.twitter.metricbot.tweetfinder.initialdelay:5s}",
      fixedRate = "${io.github.bmhm.twitter.metricbot.tweetfinder.rate:5s}"
  )
  public void findNewTweets() {
    LOG.debug("Loading tweets.");
    // TODO: Converters
    final String searchTerms = this.converter.getSerchTerms();
    LOG.debug("Search terms: [{}].", searchTerms);
    final Query query = new Query("lang:en " + searchTerms);
    query.setLang("en");
    query.setResultType(ResultType.recent);
    query.setCount(50);
    query.setSince(LocalDate.now().toString());

    try {
      final QueryResult queryResult = this.twitter.search(query);
      findMatching(queryResult);
    } catch (final TwitterException twitterEx) {
      LOG.error("unable to retrieve tweets!", twitterEx);
    }
  }

  private void findNextTweets(final QueryResult oldQueryResult) {
    if (!oldQueryResult.hasNext()) {
      return;
    }

    try {
      final QueryResult queryResult = this.twitter.search(oldQueryResult.nextQuery());
      findMatching(queryResult);
    } catch (final TwitterException twitterEx) {
      LOG.error("unable to retrieve tweets!", twitterEx);
    }
  }

  private void findMatching(final QueryResult queryResult) {
    LOG.debug("Searching in [{}] tweets using [{}]", queryResult.getCount(), this.converter);

    final List<Status> availableTweets = queryResult.getTweets().stream()
        .filter(tweet -> !this.tweetRepository.existsById(tweet.getId()))
        .filter(this::usernameDoesNotContainBlacklistedWord)
        // max age: 60 seconds * 60 == 1h.
        .filter(tweet -> tweet.getCreatedAt().toInstant().isAfter(Instant.now().minusSeconds(60 * 60L)))
        .collect(toList());

    final Optional<Status> imperialTweet = availableTweets.stream()
        .filter(tweet -> this.converter.containsUsUnits(tweet.getText()))
        .findAny();

    if (imperialTweet.isEmpty() && !availableTweets.isEmpty()) {
      LOG.warn("Did not discover imperial units: \n{}\n]].", availableTweets.stream()
          .map(Status::getText)
          .map(text -> text.replaceAll("\n", ""))
          .collect(Collectors.joining(",\n ")));
    }

    imperialTweet.ifPresentOrElse(this::reply, () -> findNextTweets(queryResult));
  }

  private boolean usernameDoesNotContainBlacklistedWord(final Status tweet) {
    final String userName = tweet.getUser().getName();

    return ACCOUNT_NAME_WORD_BLACKLIST.stream()
        .noneMatch(blacklisted -> userName.toLowerCase(Locale.ENGLISH).contains(blacklisted.toLowerCase(Locale.ENGLISH)));
  }

  private void reply(final Status status) {
    LOG.info("Matcher for Status: [{} by {} => {}].", status.getId(), status.getUser().getName(), status.getText().replaceAll("\n", ""));
    final TweetPdo tweetPdo = new TweetPdo(status.getId(), status.getCreatedAt().toInstant());
    this.tweetRepository.save(tweetPdo);

    final String converted = this.converter.returnConverted(status.getText());
    LOG.info("4ur convenience, the metric units:\n{}.", converted);
    this.tweetRepository.update(status.getId(), new Random().nextLong(), Instant.now());
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TweetFinder.class.getSimpleName() + "[", "]")
        .add("twitter=" + this.twitter)
        .toString();
  }
}
