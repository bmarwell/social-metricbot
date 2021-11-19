package io.github.bmhm.twitter.metricbot.web.events;

import static java.util.Arrays.asList;

import io.github.bmhm.twitter.metricbot.db.dao.TweetRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;

@ApplicationScoped
public class PotentialTweetProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(PotentialTweetProcessor.class);

  private static final List<String> ACCOUNT_NAME_WORD_BLACKLIST = asList(
      "Boutique", "Crazy I Buy", "weather", "Supplements", "DealsSupply");

  /**
   * recent replies.
   */
  @Inject
  private TweetRepository tweetRepository;

  @Inject
  private UnprocessedTweetQueueHolder unprocessedTweetQueueHolder;

  @Transactional
  public void addPotentialTweet(final @Observes MentionEvent mentionEvent) {
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
      this.tweetRepository.upsert(foundTweet.getId(), -1, Instant.now());
    }

    if (containsBlockedWord(foundTweet)) {
      LOG.debug("Skipping tweet [{}] because it is from a blocked user.", foundTweet.getId());
      this.tweetRepository.upsert(foundTweet.getId(), -1, Instant.now());

      return;
    }

    this.unprocessedTweetQueueHolder.add(foundTweet);
  }


  protected boolean containsBlockedWord(final Status tweet) {
    final String userName = tweet.getUser().getName();

    return ACCOUNT_NAME_WORD_BLACKLIST.stream()
        .anyMatch(blacklisted -> userName.toLowerCase(Locale.ENGLISH)
            .contains(blacklisted.toLowerCase(Locale.ENGLISH)));
  }

}
