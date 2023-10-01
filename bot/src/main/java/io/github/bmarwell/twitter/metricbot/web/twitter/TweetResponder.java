package io.github.bmarwell.twitter.metricbot.web.twitter;

import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.Tweet;
import io.github.bmarwell.twitter.metricbot.common.TwitterConfig;
import io.github.bmarwell.twitter.metricbot.conversion.UsConversion;
import io.github.bmarwell.twitter.metricbot.db.dao.TweetRepository;
import io.github.bmarwell.twitter.metricbot.db.pdo.TweetPdo;
import io.github.bmarwell.twitter.metricbot.web.AbstractResponder;
import io.github.bmarwell.twitter.metricbot.web.events.TweetProcessRequest;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Dependent
public class TweetResponder extends AbstractResponder {

    private static final Logger LOG = LoggerFactory.getLogger(TweetResponder.class);

    /**
     * recent replies.
     */
    @Inject
    private Instance<TweetRepository> tweetRepository;

    @Inject
    private TwitterApi twitter;

    @Inject
    private TwitterHelper twitterHelper;

    @Inject
    private TwitterConfig twitterConfig;

    @Inject
    private UsConversion converter;

    @Inject
    private Instance<TweetResponder> self;

    public void onTweetFound(final @Observes TweetProcessRequest event) {
        LOG.info("Checking response to event [{}].", event);
        final var foundTweet = event.getFoundTweet();

        final Optional<TweetPdo> alreadyRespondedToMention = this.self.get().findById(foundTweet.getId());
        if (alreadyRespondedToMention.isPresent()) {
            final TweetPdo tweetPdo = alreadyRespondedToMention.orElseThrow();
            LOG.debug("Already responded: [{}]", tweetPdo);

            return;
        }

        // check for units
        tryRespond(foundTweet);
    }

    protected void tryRespond(final Tweet foundTweet) {
        // respond
        // either this tweet, or quoted or retweeted or reply to (in this order).
        final var optStatusWithUnits = getStatusWithUnits(foundTweet);
        if (optStatusWithUnits.isEmpty()) {
            LOG.debug("No units found.");
            this.self.get().upsert(foundTweet.getId(), "", Instant.now());

            return;
        }

        final var statusWithUnits = optStatusWithUnits.orElseThrow();

        final Optional<TweetPdo> optExistingResponse = this.self.get().findById(statusWithUnits.getId());

        if (optExistingResponse.isPresent()) {
            final TweetPdo existingResponse = optExistingResponse.orElseThrow();
            LOG.debug("Already responded: [{}].", existingResponse);
            final long botResponseId = existingResponse.getBotResponseId();
            this.self.get().upsert(foundTweet.getId(), Long.toString(botResponseId, 10), Instant.now());

            // reply to foundTweet with Link to botResponseId

            return;
        }

        doRespond(foundTweet, statusWithUnits);
    }

    protected void doRespond(final Tweet foundTweet, final Tweet statusWithUnits) {
        if (!this.converter.containsUsUnits(statusWithUnits.getText())) {
            LOG.error(
                    "No units found, although they were found earlier?! [{}:{}].",
                    statusWithUnits.getId(),
                    statusWithUnits.getText());

            // reply with sorry.

            return;
        }

        final String responseText = this.converter.returnConverted(statusWithUnits.getText(), "\n");

        if (responseText.endsWith(":")) {
            // found units, but did not add any conversions..?
            LOG.error(
                    "No units converted, although they were found earlier?! [{}:{}].",
                    statusWithUnits.getId(),
                    statusWithUnits.getText());

            return;
        }

        final Optional<Tweet> quotedTweet = this.twitterHelper.findQuotedTweet(foundTweet.getId());
        if (quotedTweet.isPresent() && quotedTweet.orElseThrow().getId().equals(statusWithUnits.getId())) {
            // reply to foundTweet only. Do not bother the originals post's author.
            doRespondToFirst(foundTweet, responseText);
            return;
        }

        doRespondTwoPotentallyBoth(foundTweet, statusWithUnits, responseText);
    }

    protected void doRespondTwoPotentallyBoth(
        final Tweet foundTweet, final Tweet statusWithUnits, final String responseText) {
        final String mentions = createMentions(foundTweet, statusWithUnits);
        String tweetText = mentions + responseText;
        if (mentions.length() + responseText.length() + CONVENIENCE_TEXT.length() < 280) {
            tweetText = mentions + CONVENIENCE_TEXT + responseText;
        }

        try {
            LOG.info(
                    "Sending status response to [{}]: [{}].",
                statusWithUnits.getId(),
                tweetText.replaceAll("\n", "\\\\n"));
            final var response = this.twitterHelper.createReply(statusWithUnits.getId(), tweetText);

            LOG.info("Response sent: [{}] => [{}].", response.getId(), response.getText());
            // add to repository, so we do not reply again to this.
            this.self
                    .get()
                    .upsert(
                            statusWithUnits.getId(),
                            response.getId(),
                        Instant.now());

            /* // add back later.
            // if this is a mentioned or qouted tweet, add quote to the reponse we just created.
            if (!foundTweet.getId().equals(statusWithUnits.getId())) {
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
                final Tweet hintToTranslationResponse = this.twitter.updateStatus(hintToTranslation);
                // also add the actual status with units, so we do not get mentioned multiple times.
                this.self
                        .get()
                        .upsert(
                                foundTweet.getId(),
                                hintToTranslationResponse.getId(),
                                hintToTranslationResponse.getCreatedAt().toInstant());
            }
         */
        } catch (final RuntimeException twitterException) {
            LOG.error("Unable to send reply: [{}].", tweetText, twitterException);
            this.self.get().upsert(foundTweet.getId(), "-1", Instant.now());
            if (!statusWithUnits.getId().equals(foundTweet.getId())) {
                this.self.get().upsert(statusWithUnits.getId(), "-1", Instant.now());
            }
        }
    }

    protected void doRespondToFirst(final Tweet foundTweet, final String responseText) {
        final var screenName = getScreenName(foundTweet);
        final String mentions = "@" + screenName + "\n";
        String tweetText = mentions + responseText;
        if (mentions.length() + responseText.length() + CONVENIENCE_TEXT.length() < 280) {
            tweetText = mentions + CONVENIENCE_TEXT + responseText;
        }

        final Tweet statusUpdate = this.twitterHelper.createReply(foundTweet.getId(), tweetText);

        if (screenName.equals(this.twitterConfig.getAccountName())) {
            // don't reply to self.
            return;
        }

        try {
            LOG.info(
                    "Sending status response to [{}]: [{}].",
                    statusUpdate.getInReplyToStatusId(),
                    statusUpdate.getStatus().replaceAll("\n", "\\\\n"));
            final Status response = this.twitter.updateStatus(statusUpdate);
            LOG.info(
                    "Response sent: [{}] => [{}].",
                    response.getId(),
                    response.getText().replaceAll("\n", "\\\\n"));
            this.self.get().upsert(foundTweet.getId(), response.getId(), Instant.now());
        } catch (final TwitterException twitterException) {
            LOG.error(
                    "Unable to send reply: [{}].", statusUpdate.toString().replaceAll("\n", "\\\\n"), twitterException);
            this.self.get().upsert(foundTweet.getId(), -1, Instant.now());
        }
    }

    private String getScreenName(final Tweet foundTweet) {
        final String authorId = foundTweet.getAuthorId();
        return this.twitterHelper.getTwitterUserById(authorId).screenNameHandle();
    }

    private String createMentions(final Tweet foundTweet, final Tweet statusWithUnits) {
        final var foundTweetUser = this.twitterHelper.getTwitterUserById(foundTweet.getAuthorId());
        final var statusWithUnitsUser = this.twitterHelper.getTwitterUserById(statusWithUnits.getAuthorId());

        return Stream.of(
                "@" + foundTweetUser.screenNameHandle(),
                "@" + statusWithUnitsUser.screenNameHandle())
                        .distinct()
                        .collect(Collectors.joining(" "))
                + "\n";
    }

    protected Optional<Tweet> getStatusWithUnits(final Tweet foundTweet) {
        // tweet itself?
        if (containsUnits(foundTweet).isPresent() && isByOtherUser(foundTweet)) {
            LOG.info("Tweet itself contains units.");
            return Optional.of(foundTweet);
        }

        // quoted?
        final Optional<Tweet> quotedStatus = containsUnits(foundTweet.getQuotedStatus());
        if (quotedStatus.isPresent() && isByOtherUser(quotedStatus.orElseThrow())) {
            return quotedStatus;
        }

        // is retweet?
        final Optional<Tweet> retweetStatusWithUnits = containsUnits(foundTweet.getRetweetedStatus());
        if (foundTweet.isRetweet()
                && retweetStatusWithUnits.isPresent()
                && isByOtherUser(retweetStatusWithUnits.orElseThrow())) {
            return Optional.of(foundTweet.getRetweetedStatus());
        }

        // reply to?
        final long inReplyToStatusId = foundTweet.getInReplyToStatusId();
        final Optional<Tweet> replyToStatus = containsUnits(inReplyToStatusId);
        if (inReplyToStatusId != 0L && replyToStatus.isPresent() && isByOtherUser(replyToStatus.orElseThrow())) {
            return replyToStatus;
        }

        return Optional.empty();
    }

    private boolean isByOtherUser(final Tweet foundTweet) {
        final var user = this.twitterHelper.getTwitterUserById(foundTweet.getAuthorId());
        return !this.twitterConfig
            .getAccountName()
            .contains(user.screenNameHandle());
    }

    protected Optional<Tweet> containsUnits(final @Nullable String inReplyToStatusId) {
        if (inReplyToStatusId == null || inReplyToStatusId.isEmpty()) {
            return Optional.empty();
        }

        final var status = this.twitterHelper.getTweet(inReplyToStatusId);

        if (containsUnits(status).isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(status);
    }

    protected Optional<Tweet> containsUnits(final Tweet otherStatus) {
        if (otherStatus == null) {
            return Optional.empty();
        }

        if (this.converter.containsUsUnits(otherStatus.getText())) {
            return Optional.of(otherStatus);
        }

        return Optional.empty();
    }

    @Transactional
    public void upsert(final String foundTweetId, final String responseId, final Instant responseTime) {
        this.tweetRepository.get().upsert(
            Long.parseLong(foundTweetId),
            Long.parseLong(responseId),
            responseTime
        );
    }

    @Transactional
    private Optional<TweetPdo> findById(final String id) {
        return this.tweetRepository.get().findById(Long.parseLong(id));
    }

    public void setConverter(final UsConversion converter) {
        this.converter = converter;
    }
}
