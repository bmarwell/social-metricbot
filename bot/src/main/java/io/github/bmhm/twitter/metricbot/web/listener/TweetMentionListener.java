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

package io.github.bmhm.twitter.metricbot.web.listener;

import static java.util.Arrays.asList;

import io.github.bmhm.twitter.metricbot.common.TwitterConfig;
import io.github.bmhm.twitter.metricbot.db.dao.TweetRepository;
import io.github.bmhm.twitter.metricbot.web.events.MentionEvent;
import io.github.bmhm.twitter.metricbot.web.events.UnprocessedTweetQueueHolder;
import io.github.bmhm.twitter.metricbot.web.factory.TwitterProducer;
import io.github.bmhm.twitter.metricbot.web.twitter.MentionEventHandler;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.AsyncTwitter;
import twitter4j.Status;

@WebListener
public class TweetMentionListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(TweetMentionListener.class);

  @Resource
  private ManagedScheduledExecutorService scheduler;

  /**
   * recent replies.
   */
  @Inject
  private TweetRepository tweetRepository;

  @Inject
  private TwitterProducer twitterProducer;

  @Inject
  private TwitterConfig twitterConfig;

  @Inject
  private MentionEventHandler mentionEventHandler;

  @Inject
  private UnprocessedTweetQueueHolder unprocessedTweetQueueHolder;

  private AsyncTwitter asyncTwitter;

  private static final List<String> ACCOUNT_NAME_WORD_BLACKLIST = asList(
      "Boutique", "Crazy I Buy", "weather", "Supplements", "DealsSupply");

  public TweetMentionListener() {
    // injection constructor
  }

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    ServletContextListener.super.contextInitialized(sce);
    LOG.info("init: [{}].", this);
    this.asyncTwitter = this.twitterProducer.getAsyncTwitter().getInstance();
    this.asyncTwitter.addListener(this.mentionEventHandler);

    // set up scheduler
    this.scheduler.scheduleWithFixedDelay(
        this::retrieveMentions,
        this.twitterConfig.getTweetFinderInitialDelay(),
        this.twitterConfig.getTweetFinderRetrieveRate(),
        TimeUnit.SECONDS
    );
  }

  protected void retrieveMentions() {
    this.asyncTwitter.getMentions();
  }

  @Transactional
  protected void addPotentialTweet(final @ObservesAsync MentionEvent mentionEvent) {
    LOG.debug("Processing potential mention: [{}].", mentionEvent);
    final Status foundTweet = mentionEvent.getFoundTweet();

    if (this.tweetRepository.findById(foundTweet.getId()).isPresent()) {
      LOG.debug("Skipping tweet [{}] because it was already replied to.", foundTweet.getId());
      return;
    }

    if (this.unprocessedTweetQueueHolder.contains(foundTweet)) {
      LOG.info("Skipping tweet [{}] because it will be processed soon.", foundTweet.getId());
      return;
    }

    // do not check BEFORE the above.
    final Instant createdAt = foundTweet.getCreatedAt().toInstant();
    // only reply to mentions in the last 10 minutes
    if (createdAt.isBefore(Instant.now().minusSeconds(60 * 10L))) {
      LOG.info("Skipping tweet [{}] because it is too old: [{}].", foundTweet.getId(), createdAt);
      this.tweetRepository.save(foundTweet.getId(), -1, Instant.now());
    }

    if (containsBlockedWord(foundTweet)) {
      LOG.debug("Skipping tweet [{}] because it is from a blocked user.", foundTweet.getId());
      this.tweetRepository.save(foundTweet.getId(), -1, Instant.now());

      return;
    }

    this.unprocessedTweetQueueHolder.add(foundTweet);
  }

  protected boolean containsBlockedWord(final Status tweet) {
    final String userName = tweet.getUser().getName();

    return ACCOUNT_NAME_WORD_BLACKLIST.stream()
        .anyMatch(blacklisted -> userName.toLowerCase(Locale.ENGLISH).contains(blacklisted.toLowerCase(Locale.ENGLISH)));
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TweetMentionListener.class.getSimpleName() + "[", "]")
        .add("scheduler=" + this.scheduler)
        .add("tweetRepository=" + this.tweetRepository)
        .add("twitterProducer=" + this.twitterProducer)
        .add("twitterConfig=" + this.twitterConfig)
        .add("mentionEventHandler=" + this.mentionEventHandler)
        .add("unprocessedTweetQueueHolder=" + this.unprocessedTweetQueueHolder)
        .add("asyncTwitter=" + this.asyncTwitter)
        .toString();
  }
}
