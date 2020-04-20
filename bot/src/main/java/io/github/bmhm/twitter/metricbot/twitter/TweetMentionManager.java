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


import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import io.github.bmhm.twitter.metricbot.db.dao.TweetRepository;
import io.github.bmhm.twitter.metricbot.events.MentionEvent;
import io.github.bmhm.twitter.metricbot.events.TweetProcessRequest;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;

@Singleton
public class TweetMentionManager {

  private static final Logger LOG = LoggerFactory.getLogger(TweetMentionManager.class);

  @Inject
  private ApplicationEventPublisher eventPublisher;

  /**
   * recent replies.
   */
  @Inject
  private TweetRepository tweetRepository;

  @Inject
  private AsyncTwitterFactory asyncTwitterFactory;

  private AsyncTwitter asyncTwitter;

  private final ArrayDeque<Status> processItems = new ArrayDeque<>();

  private static final List<String> ACCOUNT_NAME_WORD_BLACKLIST = asList(
      "Boutique", "Crazy I Buy", "weather", "Supplements", "DealsSupply");

  public TweetMentionManager() {
    // injection constructor
  }

  @EventListener
  protected void onStartup(final StartupEvent event) {
    LOG.info("init: [{}].", this);
    this.asyncTwitter = this.asyncTwitterFactory.getInstance();
    final MentionListener mentionListener = new MentionListener(this.eventPublisher);
    this.asyncTwitter.addListener(mentionListener);
  }

  @Scheduled(initialDelay = "5s", fixedDelay = "10s")
  protected void retrieveMentions() {
    this.asyncTwitter.getMentions();
  }

  @Async
  @EventListener
  protected void addPotentialTweet(final MentionEvent mentionEvent) {
    LOG.info("Processing potential mention: [{}].", mentionEvent);
    final Status foundTweet = mentionEvent.getFoundTweet();

    if (this.tweetRepository.findById(foundTweet.getId()).isPresent()) {
      LOG.info("Skipping tweet [{}] because it was already replied to.", foundTweet.getId());
      return;
    }

    if (this.processItems.contains(foundTweet)) {
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
      LOG.info("Skipping tweet [{}] because it is from a blocked user.", foundTweet.getId());
      this.tweetRepository.save(foundTweet.getId(), -1, Instant.now());

      return;
    }

    this.processItems.add(foundTweet);
  }

  /**
   * Only emit a new tweet once every 5 seconds.
   */
  @Scheduled(
      initialDelay = "${io.github.bmhm.twitter.metricbot.tweetfinder.initialdelay:5s}",
      fixedRate = "${io.github.bmhm.twitter.metricbot.tweetfinder.rate:5s}"
  )
  protected void emitMention() {
    synchronized (this.processItems) {
      if (this.processItems.isEmpty()) {
        LOG.debug("No tweet to reply to.");
        return;
      }

      final Status foundTweet = this.processItems.poll();
      LOG.info("Emitting event for tweet: [{}].", foundTweet);

      this.eventPublisher.publishEvent(new TweetProcessRequest(this, foundTweet));
    }
  }

  protected boolean containsBlockedWord(final Status tweet) {
    final String userName = tweet.getUser().getName();

    return ACCOUNT_NAME_WORD_BLACKLIST.stream()
        .anyMatch(blacklisted -> userName.toLowerCase(Locale.ENGLISH).contains(blacklisted.toLowerCase(Locale.ENGLISH)));
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TweetMentionManager.class.getSimpleName() + "[", "]")
        .add("tweetRepository=" + this.tweetRepository)
        .add("eventPublisher=" + this.eventPublisher)
        .add("processItems=" + this.processItems)
        .toString();
  }
}
