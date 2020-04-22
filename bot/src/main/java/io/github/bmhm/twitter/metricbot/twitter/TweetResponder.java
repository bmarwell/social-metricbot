package io.github.bmhm.twitter.metricbot.twitter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.bmhm.twitter.metricbot.conversion.UsConversion;
import io.github.bmhm.twitter.metricbot.db.dao.TweetRepository;
import io.github.bmhm.twitter.metricbot.db.pdo.TweetPdo;
import io.github.bmhm.twitter.metricbot.events.TweetProcessRequest;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@Singleton
public class TweetResponder {

  private static final Logger LOG = LoggerFactory.getLogger(TweetResponder.class);
  private static final String CONVENIENCE_TEXT = "For your convenience, the metric units:\n";

  /**
   * recent replies.
   */
  @Inject
  private TweetRepository tweetRepository;

  @Inject
  private Twitter twitter;

  @Inject
  private UsConversion converter;

  @EventListener
  @Async
  public void onTweetFound(final TweetProcessRequest event) {
    LOG.info("Checking response to event [{}].", event);
    final Status foundTweet = event.getFoundTweet();

    final Optional<TweetPdo> alreadyRespondedToMention = this.tweetRepository.findById(foundTweet.getId());
    if (alreadyRespondedToMention.isPresent()) {
      final TweetPdo tweetPdo = alreadyRespondedToMention.orElseThrow();
      LOG.info("Already responded: [{}]", tweetPdo);

      return;
    }

    // check for units
    tryRespond(foundTweet);

  }

  protected void tryRespond(final Status foundTweet) {
    // respond
    // either this tweet, or quoted or retweeted or reply to (in this order).
    final Optional<Status> optStatusWithUnits = getStatusWithUnits(foundTweet);
    if (optStatusWithUnits.isEmpty()) {
      LOG.info("No units found.");

      // reply with sorry
      final StatusUpdate statusUpdate =
          new StatusUpdate("Sorry, I did not find any units in either your status nor in a quoted, retweeted or mentioned status.")
              .inReplyToStatusId(foundTweet.getId());

      try {
        final Status status = this.twitter.updateStatus(statusUpdate);
        final long botResponseId = status.getId();
        this.tweetRepository.save(foundTweet.getId(), botResponseId, Instant.now());
      } catch (final TwitterException twitterException) {
        this.tweetRepository.save(foundTweet.getId(), -1, Instant.now());
      }

      return;
    }

    final Status statusWithUnits = optStatusWithUnits.orElseThrow();
    
    final Optional<TweetPdo> optExistingResponse = this.tweetRepository.findById(statusWithUnits.getId());

    if (optExistingResponse.isPresent()) {
      final TweetPdo existingResponse = optExistingResponse.orElseThrow();
      LOG.info("Already responded: [{}].", existingResponse);
      final long botResponseId = existingResponse.getBotResponseId();
      this.tweetRepository.save(foundTweet.getId(), botResponseId, Instant.now());

      // reply to foundTweet with Link to botResponseId

      return;
    }

    doRespond(foundTweet, statusWithUnits);
  }

  protected void doRespond(final Status foundTweet, final Status statusWithUnits) {
    if (!this.converter.containsUsUnits(statusWithUnits.getText())) {
      LOG.error("No units found, although they were found earlier?! [{}].", statusWithUnits.getText());

      // reply with sorry.

      return;
    }

    final String responseText = this.converter.returnConverted(statusWithUnits.getText(), "\n");
    final String mentions = createMentions(foundTweet, statusWithUnits);
    String tweetText = mentions + responseText;
    if (mentions.length() + responseText.length() + CONVENIENCE_TEXT.length() < 280) {
      tweetText = mentions + CONVENIENCE_TEXT + responseText;
    }

    final StatusUpdate statusUpdate = new StatusUpdate(tweetText)
        .inReplyToStatusId(statusWithUnits.getId());

    try {
      LOG.info("Sending status response: [{}].", statusUpdate);
      final Status response = this.twitter.updateStatus(statusUpdate);
      LOG.info("Response sent: [{}].", response);
      // add to repository so we do not reply again to this.
      this.tweetRepository.save(statusWithUnits.getId(), response.getId(), response.getCreatedAt().toInstant());

      // if this is a mentioned or qouted tweet, add quote to the reponse we just created.
      if (foundTweet.getId() != statusWithUnits.getId()) {
        // show the requester the tweet we created.
        final String url =
            String.format(Locale.ENGLISH, "https://twitter.com/%s/status/%d", response.getUser().getScreenName(), response.getId());

        final StatusUpdate hintToTranslation =
            new StatusUpdate(String.format(Locale.ENGLISH, "@%s\nHere you go:\n\n%s\n", foundTweet.getUser().getScreenName(), url))
                .inReplyToStatusId(foundTweet.getId());
        final Status hintToTranslationResponse = this.twitter.updateStatus(hintToTranslation);
        // also add the actual status with units, so we do not get mentioned multiple times.
        this.tweetRepository
            .save(foundTweet.getId(), hintToTranslationResponse.getId(), hintToTranslationResponse.getCreatedAt().toInstant());
      }
    } catch (final TwitterException twitterException) {
      LOG.error("Unable to send reply: [{}].", statusUpdate, twitterException);
      this.tweetRepository.save(foundTweet.getId(), -1, Instant.now());
      this.tweetRepository.save(statusWithUnits.getId(), -1, Instant.now());
    }
  }

  private String createMentions(final Status foundTweet, final Status statusWithUnits) {
    return Stream.of(
        "@" + foundTweet.getUser().getScreenName(),
        "@" + statusWithUnits.getUser().getScreenName()
    )
        .distinct()
        .collect(Collectors.joining(" ")) + "\n";
  }

  protected Optional<Status> getStatusWithUnits(final Status foundTweet) {
    // tweet itself?
    if (containsUnits(foundTweet).isPresent()) {
      LOG.info("Tweet itself contains units.");
      return Optional.ofNullable(foundTweet);
    }

    // quoted?
    final Optional<Status> quotedStatus = containsUnits(foundTweet.getQuotedStatus());
    if (quotedStatus.isPresent()) {
      return quotedStatus;
    }

    // is retweet?
    final Optional<Status> retweetStatusWithUnits = containsUnits(foundTweet.getRetweetedStatus());
    if (foundTweet.isRetweet() && retweetStatusWithUnits.isPresent()) {
      return Optional.of(foundTweet.getRetweetedStatus());
    }

    // reply to?
    final long inReplyToStatusId = foundTweet.getInReplyToStatusId();
    final Optional<Status> replyToStatus = containsUnits(inReplyToStatusId);
    if (inReplyToStatusId != 0L && replyToStatus.isPresent()) {
      return replyToStatus;
    }

    return Optional.empty();
  }

  protected Optional<Status> containsUnits(final long inReplyToStatusId) {
    if (inReplyToStatusId == 0L || inReplyToStatusId == -1L) {
      return Optional.empty();
    }

    try {
      final Status status = this.twitter.showStatus(inReplyToStatusId);
      if (containsUnits(status).isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(status);
    } catch (final TwitterException twitterEx) {
      LOG.error("unable to retrieve tweet ID=[" + inReplyToStatusId + "].", twitterEx);
    }

    return Optional.empty();
  }

  protected Optional<Status> containsUnits(final @Nullable Status otherStatus) {
    if (otherStatus == null) {
      return Optional.empty();
    }

    if (this.converter.containsUsUnits(otherStatus.getText())) {
      return Optional.of(otherStatus);
    }

    return Optional.empty();
  }
}
