package io.github.bmarwell.social.metricbot.web.bsky.processing;

import io.github.bmarwell.social.metricbot.bsky.BlueSkyClient;
import io.github.bmarwell.social.metricbot.bsky.BskyStatus;
import io.github.bmarwell.social.metricbot.bsky.json.BskyResponseDraft;
import io.github.bmarwell.social.metricbot.conversion.UsConversion;
import io.github.bmarwell.social.metricbot.db.dao.BskyStatusRepository;
import io.github.bmarwell.social.metricbot.db.pdo.BskyStatusPdo;
import io.github.bmarwell.social.metricbot.web.AbstractResponder;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will actually respond to statuses wrapped in a ReplyRequest.
 */
@Dependent
public class BskyResponder extends AbstractResponder implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(BskyResponder.class);

    @Serial
    private static final long serialVersionUID = -6151380892941404038L;

    @Inject
    private Instance<BskyResponder> self;

    @Inject
    private Instance<BskyStatusRepository> repository;

    @Inject
    private BlueSkyClient bskyClient;

    @Inject
    private UsConversion converter;

    public void onMastodonStatusFound(@ObservesAsync final BskyProcessRequest event) {
        LOG.debug("Checking response to event [{}].", event);
        final BskyStatus status = event.status();

        final Optional<BskyStatusPdo> alreadyRespondedToMention =
                this.self.get().findById(status.uri());
        if (alreadyRespondedToMention.isPresent()) {
            final BskyStatusPdo tweetPdo = alreadyRespondedToMention.orElseThrow();
            LOG.debug("Already responded: [{}]", tweetPdo);

            return;
        }

        LOG.debug("Trying to respond now.");

        // check for units
        tryRespond(status);
    }

    private void tryRespond(final BskyStatus status) {
        // respond
        // either this tweet, or quoted or retweeted or reply to (in this order).
        final Optional<BskyStatus> optStatusWithUnits = getStatusWithUnits(status);
        if (optStatusWithUnits.isEmpty()) {
            LOG.debug("No units found.");
            this.self.get().upsert(status.uri(), status.createdAt(), null, Instant.now());

            return;
        }

        final BskyStatus statusWithUnits = optStatusWithUnits.orElseThrow();

        final Optional<BskyStatusPdo> optExistingResponse = this.self.get().findById(statusWithUnits.uri());

        if (optExistingResponse.isPresent()) {
            final BskyStatusPdo existingResponse = optExistingResponse.orElseThrow();
            LOG.debug("Already responded: [{}].", existingResponse);
            // final URI botResponseAtUri = existingResponse.getBotResponseAtUri();
            // this.self.get().upsert(status.uri(), status.createdAt(), botResponseAtUri, Instant.now());

            // reply to foundTweet with Link to botResponseAtUri

            return;
        }

        LOG.debug("no existing response known.");

        try {
            doRespond(status, statusWithUnits);
        } catch (final Throwable ex) {
            LOG.error("Unexpected problem", ex);
        }
    }

    private void doRespond(final BskyStatus status, final BskyStatus statusWithUnits) {
        if (!this.converter.containsUsUnits(statusWithUnits.text())) {
            LOG.error(
                    "No units found, although they were found earlier?! [{}:{}]",
                    statusWithUnits.uri(),
                    statusWithUnits.text());

            return;
        }

        final var responseText = this.converter.returnConverted(statusWithUnits.text(), "\n");

        if (responseText.endsWith(":")) {
            LOG.error(
                    "No units converted, although they were found earlier?! [{}:{}]",
                    statusWithUnits.uri(),
                    statusWithUnits.text());

            return;
        }

        if (status.uri().equals(statusWithUnits.uri())) {
            LOG.info("Only respond to first");
            doRespondToFirst(status, responseText);
            return;
        }

        LOG.info("Responding to potentially both.");

        doRespondToPotentiallyBoth(status, statusWithUnits, responseText);
    }

    private void doRespondToFirst(final BskyStatus status, final String responseText) {
        final var fullResponseText = CONVENIENCE_TEXT + responseText;
        final var statusDraft = new BskyResponseDraft(fullResponseText, status);

        LOG.info("Sending response: " + statusDraft);
        this.self.get().sendOrLog(status, statusDraft);
    }

    private void doRespondToPotentiallyBoth(
            final BskyStatus foundStatus, final BskyStatus statusWithUnits, final String responseText) {
        if (!this.bskyClient.isByOtherUser(statusWithUnits)) {
            // don't respond to self
            LOG.info("Don't respond to self.");

            return;
        }

        final var fullResponseText = CONVENIENCE_TEXT + responseText;
        final var statusDraft = new BskyResponseDraft(fullResponseText, statusWithUnits);

        LOG.info("Sending response: " + statusDraft);
        final Optional<BskyStatus> sentReply = this.self.get().sendOrLog(statusWithUnits, statusDraft);

        if (foundStatus.uri().equals(statusWithUnits.uri())) {
            return;
        }

        if (sentReply.isEmpty()) {
            return;
        }

        final var conversionPostUri = this.bskyClient.getStatusUri(sentReply.orElseThrow());

        final String hereYouGoText = String.format("Here you go: \n\n%s\n", conversionPostUri);
        final var hereYouGoDraft = new BskyResponseDraft(hereYouGoText, foundStatus);
        sendHintOrTimeout(hereYouGoDraft);
    }

    private Optional<BskyStatus> sendHintOrTimeout(final BskyResponseDraft hereYouGoDraft) {
        LOG.info(
                "Sending status response to quoter [{}]: [{}].",
                hereYouGoDraft.postToReplyTo().uri(),
                hereYouGoDraft.postStatus().replaceAll("\n", "\\\\n"));
        return this.bskyClient.sendReply(hereYouGoDraft);
    }

    private Optional<BskyStatus> sendOrLog(final BskyStatus statusWithUnits, final BskyResponseDraft statusDraft) {
        try {
            final Optional<BskyStatus> responseStatus = this.bskyClient.sendReply(statusDraft);

            if (responseStatus.isEmpty()) {
                LOG.error("Unable to send reply: [{}].", statusDraft);
                this.self.get().upsert(statusWithUnits.uri(), statusWithUnits.createdAt(), null, Instant.now());

                return responseStatus;
            }

            final var response = responseStatus.orElseThrow();
            LOG.info("Response sent: [{}] => [{}].", response.uri(), response.text());
            this.self
                    .get()
                    .upsert(statusWithUnits.uri(), statusWithUnits.createdAt(), response.uri(), response.createdAt());

            return Optional.of(response);
        } catch (final Throwable ex) {
            LOG.error("Unknown error when responding to post.", ex);

            return Optional.empty();
        }
    }

    private Optional<BskyStatus> getStatusWithUnits(final BskyStatus status) {
        // tweet itself?
        if (containsUnits(status) && this.bskyClient.isByOtherUser(status)) {
            LOG.debug("Post itself contains units.");
            return Optional.of(status);
        }

        // boosted? Same as quoted and retweeted/reblogged for Mastodon.
        if (status.isQuoting()) {
            final BskyStatus rebloggedStatus =
                    this.bskyClient.getRepostedStatus(status).orElseThrow();

            if (containsUnits(rebloggedStatus) && this.bskyClient.isByOtherUser(rebloggedStatus)) {
                LOG.debug("found units in post which was replied to.");
                return Optional.of(rebloggedStatus);
            }
        }

        // reply to?
        if (status.isReply()) {
            final Optional<BskyStatus> repliedTo = this.bskyClient.getRepliedToPost(status);

            if (repliedTo.isEmpty()) {
                return Optional.empty();
            }

            final BskyStatus replyToStatus = repliedTo.orElseThrow();
            if (containsUnits(replyToStatus) && this.bskyClient.isByOtherUser(replyToStatus)) {
                LOG.debug("found units in post which was replied to.");
                return Optional.of(replyToStatus);
            }
        }

        return Optional.empty();
    }

    private boolean containsUnits(final BskyStatus status) {
        return this.converter.containsUsUnits(status.text());
    }

    @Transactional
    public void upsert(
            final URI postAtUri,
            final Instant postTime,
            final @Nullable URI botResponseAtUri,
            final Instant responseTime) {
        this.repository.get().upsert(postAtUri, postTime, botResponseAtUri, responseTime);
    }

    @Transactional
    private Optional<BskyStatusPdo> findById(final URI postAtUri) {
        return this.repository.get().findByAtUri(postAtUri);
    }

    public Instance<BskyResponder> getSelf() {
        return self;
    }

    public void setSelf(final Instance<BskyResponder> self) {
        this.self = self;
    }

    public Instance<BskyStatusRepository> getRepository() {
        return repository;
    }

    public void setRepository(final Instance<BskyStatusRepository> repository) {
        this.repository = repository;
    }

    public BlueSkyClient getBskyClient() {
        return bskyClient;
    }

    public void setBskyClient(final BlueSkyClient bskyClient) {
        this.bskyClient = bskyClient;
    }

    public UsConversion getConverter() {
        return converter;
    }

    public void setConverter(final UsConversion converter) {
        this.converter = converter;
    }
}
