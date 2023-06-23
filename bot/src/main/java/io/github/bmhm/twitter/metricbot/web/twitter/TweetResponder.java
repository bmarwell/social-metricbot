package io.github.bmhm.twitter.metricbot.web.twitter;

import io.github.bmhm.twitter.metricbot.common.TwitterConfig;
import io.github.bmhm.twitter.metricbot.conversion.UsConversion;
import io.github.bmhm.twitter.metricbot.db.dao.TweetRepository;
import io.github.bmhm.twitter.metricbot.db.pdo.TweetPdo;
import io.github.bmhm.twitter.metricbot.web.events.TweetProcessRequest;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@Dependent
public class TweetResponder {

    private static final Logger LOG = LoggerFactory.getLogger(TweetResponder.class);
    private static final String CONVENIENCE_TEXT = "For your convenience, the metric units:\n";

    /**
     * recent replies.
     */
    @Inject
    private Instance<TweetRepository> tweetRepository;

    @Inject
    private Twitter twitter;

    @Inject
    private TwitterConfig twitterConfig;

    @Inject
    private UsConversion converter;

    @Inject
    private Instance<TweetResponder> self;

    public void onTweetFound(final @Observes TweetProcessRequest event) {
        LOG.info("Checking response to event [{}].", event);
        final Status foundTweet = event.getFoundTweet();

        final Optional<TweetPdo> alreadyRespondedToMention = this.self.get().findById(foundTweet.getId());
        if (alreadyRespondedToMention.isPresent()) {
            final TweetPdo tweetPdo = alreadyRespondedToMention.orElseThrow();
            LOG.debug("Already responded: [{}]", tweetPdo);

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
            LOG.debug("No units found.");
            this.self.get().upsert(foundTweet.getId(), -1, Instant.now());

            return;
        }

        final Status statusWithUnits = optStatusWithUnits.orElseThrow();

        final Optional<TweetPdo> optExistingResponse = this.self.get().findById(statusWithUnits.getId());

        if (optExistingResponse.isPresent()) {
            final TweetPdo existingResponse = optExistingResponse.orElseThrow();
            LOG.debug("Already responded: [{}].", existingResponse);
            final long botResponseId = existingResponse.getBotResponseId();
            this.self.get().upsert(foundTweet.getId(), botResponseId, Instant.now());

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

        if (foundTweet.getQuotedStatusId() == statusWithUnits.getId()) {
            // reply to foundTweet only. Do not bother the originals post's author.
            doRespondToFirst(foundTweet, responseText);
            return;
        }

        doRespondTwoPotentallyBoth(foundTweet, statusWithUnits, responseText);
    }

    private void doRespondTwoPotentallyBoth(
            final Status foundTweet, final Status statusWithUnits, final String responseText) {
        final String mentions = createMentions(foundTweet, statusWithUnits);
        String tweetText = mentions + responseText;
        if (mentions.length() + responseText.length() + CONVENIENCE_TEXT.length() < 280) {
            tweetText = mentions + CONVENIENCE_TEXT + responseText;
        }

        final StatusUpdate statusUpdate = new StatusUpdate(tweetText).inReplyToStatusId(statusWithUnits.getId());

        try {
            LOG.info(
                    "Sending status response to [{}]: [{}].",
                    statusUpdate.getInReplyToStatusId(),
                    statusUpdate.getStatus());
            final Status response = this.twitter.updateStatus(statusUpdate);
            LOG.info("Response sent: [{}] => [{}].", response.getId(), response.getText());
            // add to repository so we do not reply again to this.
            this.self
                    .get()
                    .upsert(
                            statusWithUnits.getId(),
                            response.getId(),
                            response.getCreatedAt().toInstant());

            // if this is a mentioned or qouted tweet, add quote to the reponse we just created.
            if (foundTweet.getId() != statusWithUnits.getId()) {
                // show the requester the tweet we created.
                final String url = String.format(
                        Locale.ENGLISH,
                        "https://twitter.com/%s/status/%d",
                        response.getUser().getScreenName(),
                        response.getId());

                final StatusUpdate hintToTranslation = new StatusUpdate(String.format(
                                Locale.ENGLISH,
                                "@%s\nHere you go:\n\n%s\n",
                                foundTweet.getUser().getScreenName(),
                                url))
                        .inReplyToStatusId(foundTweet.getId());
                final Status hintToTranslationResponse = this.twitter.updateStatus(hintToTranslation);
                // also add the actual status with units, so we do not get mentioned multiple times.
                this.self
                        .get()
                        .upsert(
                                foundTweet.getId(),
                                hintToTranslationResponse.getId(),
                                hintToTranslationResponse.getCreatedAt().toInstant());
            }
        } catch (final TwitterException twitterException) {
            LOG.error("Unable to send reply: [{}].", statusUpdate, twitterException);
            this.self.get().upsert(foundTweet.getId(), -1, Instant.now());
            if (statusWithUnits.getId() != foundTweet.getId()) {
                this.self.get().upsert(statusWithUnits.getId(), -1, Instant.now());
            }
        }
    }

    protected void doRespondToFirst(final Status foundTweet, final String responseText) {
        final String mentions = "@" + foundTweet.getUser().getScreenName() + "\n";
        String tweetText = mentions + responseText;
        if (mentions.length() + responseText.length() + CONVENIENCE_TEXT.length() < 280) {
            tweetText = mentions + CONVENIENCE_TEXT + responseText;
        }

        final StatusUpdate statusUpdate = new StatusUpdate(tweetText).inReplyToStatusId(foundTweet.getId());

        try {
            LOG.info(
                    "Sending status response to [{}]: [{}].",
                    statusUpdate.getInReplyToStatusId(),
                    statusUpdate.getStatus());
            final Status response = this.twitter.updateStatus(statusUpdate);
            LOG.info("Response sent: [{}] => [{}].", response.getId(), response.getText());
            this.self.get().upsert(foundTweet.getId(), response.getId(), Instant.now());
        } catch (final TwitterException twitterException) {
            LOG.error("Unable to send reply: [{}].", statusUpdate, twitterException);
            this.self.get().upsert(foundTweet.getId(), -1, Instant.now());
        }
    }

    private String createMentions(final Status foundTweet, final Status statusWithUnits) {
        return Stream.of(
                                "@" + foundTweet.getUser().getScreenName(),
                                "@" + statusWithUnits.getUser().getScreenName())
                        .distinct()
                        .collect(Collectors.joining(" "))
                + "\n";
    }

    protected Optional<Status> getStatusWithUnits(final Status foundTweet) {
        // tweet itself?
        if (containsUnits(foundTweet).isPresent() && isByOtherUser(foundTweet)) {
            LOG.info("Tweet itself contains units.");
            return Optional.of(foundTweet);
        }

        // quoted?
        final Optional<Status> quotedStatus = containsUnits(foundTweet.getQuotedStatus());
        if (quotedStatus.isPresent() && isByOtherUser(quotedStatus.orElseThrow())) {
            return quotedStatus;
        }

        // is retweet?
        final Optional<Status> retweetStatusWithUnits = containsUnits(foundTweet.getRetweetedStatus());
        if (foundTweet.isRetweet()
                && retweetStatusWithUnits.isPresent()
                && isByOtherUser(retweetStatusWithUnits.orElseThrow())) {
            return Optional.of(foundTweet.getRetweetedStatus());
        }

        // reply to?
        final long inReplyToStatusId = foundTweet.getInReplyToStatusId();
        final Optional<Status> replyToStatus = containsUnits(inReplyToStatusId);
        if (inReplyToStatusId != 0L && replyToStatus.isPresent() && isByOtherUser(replyToStatus.orElseThrow())) {
            return replyToStatus;
        }

        return Optional.empty();
    }

    private boolean isByOtherUser(final Status foundTweet) {
        return !this.twitterConfig
                .getAccountName()
                .contains(foundTweet.getUser().getName());
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

    protected Optional<Status> containsUnits(final Status otherStatus) {
        if (otherStatus == null) {
            return Optional.empty();
        }

        if (this.converter.containsUsUnits(otherStatus.getText())) {
            return Optional.of(otherStatus);
        }

        return Optional.empty();
    }

    @Transactional
    public void upsert(final long foundTweetId, final long responseId, final Instant responseTime) {
        this.tweetRepository.get().upsert(foundTweetId, responseId, responseTime);
    }

    @Transactional
    private Optional<TweetPdo> findById(final long id) {
        return this.tweetRepository.get().findById(id);
    }
}
