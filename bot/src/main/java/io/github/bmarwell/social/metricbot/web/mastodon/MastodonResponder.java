package io.github.bmarwell.social.metricbot.web.mastodon;

import io.github.bmarwell.social.metricbot.common.MastodonConfig;
import io.github.bmarwell.social.metricbot.conversion.UsConversion;
import io.github.bmarwell.social.metricbot.db.dao.MastodonStatusRepository;
import io.github.bmarwell.social.metricbot.db.pdo.MastodonStatusPdo;
import io.github.bmarwell.social.metricbot.mastodon.*;
import io.github.bmarwell.social.metricbot.web.AbstractResponder;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class MastodonResponder extends AbstractResponder {

    private static final Logger LOG = LoggerFactory.getLogger(MastodonResponder.class);

    @Resource
    private ManagedExecutorService executor;

    @Inject
    private Instance<MastodonResponder> self;

    @Inject
    private Instance<MastodonStatusRepository> repository;

    @Inject
    private MastodonConfig mastodonConfig;

    @Inject
    private MastodonClient mastodonClient;

    @Inject
    private UsConversion converter;

    public void onMastodonStatusFound(@Observes MastodonProcessRequest event) {
        LOG.info("Checking response to event [{}].", event);
        final MastodonStatus status = event.status();

        final Optional<MastodonStatusPdo> alreadyRespondedToMention =
                this.self.get().findById(status.id());
        if (alreadyRespondedToMention.isPresent()) {
            final MastodonStatusPdo tweetPdo = alreadyRespondedToMention.orElseThrow();
            LOG.debug("Already responded: [{}]", tweetPdo);

            return;
        }

        // check for units
        tryRespond(status);
    }

    private void tryRespond(MastodonStatus status) {
        // respond
        // either this tweet, or quoted or retweeted or reply to (in this order).
        final Optional<MastodonStatus> optStatusWithUnits = getStatusWithUnits(status);
        if (optStatusWithUnits.isEmpty()) {
            LOG.debug("No units found.");
            this.self.get().upsert(status.id(), status.createdAt(), MastodonStatusId.empty(), Instant.now());

            return;
        }

        final MastodonStatus statusWithUnits = optStatusWithUnits.orElseThrow();

        final Optional<MastodonStatusPdo> optExistingResponse = this.self.get().findById(statusWithUnits.id());

        if (optExistingResponse.isPresent()) {
            final MastodonStatusPdo existingResponse = optExistingResponse.orElseThrow();
            LOG.debug("Already responded: [{}].", existingResponse);
            final MastodonStatusId botResponseId = new MastodonStatusId(existingResponse.getBotResponseId());
            this.self.get().upsert(status.id(), status.createdAt(), botResponseId, Instant.now());

            // reply to foundTweet with Link to botResponseId

            return;
        }

        doRespond(status, statusWithUnits);
    }

    private void doRespond(MastodonStatus status, MastodonStatus statusWithUnits) {
        if (!this.converter.containsUsUnits(statusWithUnits.rawContent())) {
            LOG.error(
                    "No units found, although they were found earlier?! [{}:{}]",
                    statusWithUnits.id(),
                    statusWithUnits.rawContent());

            return;
        }

        var responseText = this.converter.returnConverted(statusWithUnits.rawContent(), "\n");

        if (responseText.endsWith(":")) {
            LOG.error(
                    "No units converted, although they were found earlier?! [{}:{}]",
                    statusWithUnits.id(),
                    statusWithUnits.rawContent());

            return;
        }

        if (status.id().equals(statusWithUnits.id())) {
            doRespondToFirst(status, responseText);
            return;
        }

        doRespondToPotentiallyBoth(status, statusWithUnits, responseText);
    }

    private void doRespondToPotentiallyBoth(
            MastodonStatus foundStatus, MastodonStatus statusWithUnits, String responseText) {
        if (statusWithUnits
                .account()
                .acct()
                .toLowerCase(Locale.ROOT)
                .contains(this.mastodonConfig.getAccountName().toLowerCase(Locale.ROOT))) {
            // don't respond to self

            return;
        }

        var tootText = CONVENIENCE_TEXT + responseText;
        var statusDraft = new MastodonTextStatusDraft(
                tootText, statusWithUnits.id(), MastodonStatusVisiblilty.PUBLIC, MastodonStatusLanguage.ENGLISH);

        Optional<MastodonStatus> sentReply = this.self.get().sendOrLog(statusWithUnits, statusDraft);

        if (foundStatus.id().equals(statusWithUnits.id())) {
            return;
        }

        if (sentReply.isEmpty()) {
            return;
        }

        MastodonStatus reply = sentReply.orElseThrow();
        String hereYouGoText =
                String.format("Here you go: \n\n%s\n", reply.url().toString());
        var hereYouGoDraft = new MastodonTextStatusDraft(
                hereYouGoText, foundStatus.id(), MastodonStatusVisiblilty.PUBLIC, MastodonStatusLanguage.ENGLISH);
        sendTootOrTimeout(hereYouGoDraft);
    }

    private void doRespondToFirst(MastodonStatus statusWithUnits, String responseText) {
        if (statusWithUnits
                .account()
                .acct()
                .toLowerCase(Locale.ROOT)
                .contains(this.mastodonConfig.getAccountName().toLowerCase(Locale.ROOT))) {
            // don't respond to self

            return;
        }

        var tootText = /* mentions + */ CONVENIENCE_TEXT + responseText;
        var statusToSend = new MastodonTextStatusDraft(
                tootText, statusWithUnits.id(), MastodonStatusVisiblilty.PUBLIC, MastodonStatusLanguage.ENGLISH);

        this.self.get().sendOrLog(statusWithUnits, statusToSend);
    }

    @Transactional
    public Optional<MastodonStatus> sendOrLog(MastodonStatus statusWithUnits, MastodonTextStatusDraft statusToSend) {
        try {
            Optional<MastodonStatus> responseStatus = sendTootOrTimeout(statusToSend);

            if (responseStatus.isEmpty()) {
                LOG.error("Unable to send reply: [{}].", statusToSend);
                this.self
                        .get()
                        .upsert(
                                statusWithUnits.id(),
                                statusWithUnits.createdAt(),
                                MastodonStatusId.empty(),
                                Instant.now());

                return responseStatus;
            }

            MastodonStatus status = responseStatus.orElseThrow();
            LOG.info("Response sent: [{}] => [{}].", status.id(), status.rawContent());
            this.self.get().upsert(statusWithUnits.id(), statusWithUnits.createdAt(), status.id(), status.createdAt());

            return Optional.of(status);
        } catch (Throwable ex) {
            LOG.error("Unknown error when responding to toot.", ex);

            return Optional.empty();
        }
    }

    private Optional<MastodonStatus> sendTootOrTimeout(MastodonTextStatusDraft statusToSend) {
        LOG.info(
                "Sending status response to [{}]: [{}].",
                statusToSend.replyToId(),
                statusToSend.tootText().replaceAll("\n", "\\\\n"));
        MastodonClient clientCopy = this.mastodonClient;
        CompletableFuture<Optional<MastodonStatus>> handled = executor.copy(clientCopy.postStatus(statusToSend))
                .orTimeout(30, TimeUnit.SECONDS)
                .handleAsync(MastodonResponder::resultOrLog);

        return handled.join();
    }

    private static Optional<MastodonStatus> resultOrLog(Optional<MastodonStatus> result, @Nullable Throwable error) {
        if (error != null) {
            LOG.error("No result retrieved.", error);

            return Optional.<MastodonStatus>empty();
        }
        return result;
    }

    private Optional<MastodonStatus> getStatusWithUnits(MastodonStatus status) {
        // tweet itself?
        if (containsUnits(status) && isByOtherUser(status)) {
            LOG.info("Toot itself contains units.");
            return Optional.of(status);
        }

        // boosted? Same as quoted and retweeted/reblogged for Mastodon.
        if (status.isReblogged() && status.reblogged().isPresent()) {
            MastodonStatus rebloggedStatus = status.reblogged().orElseThrow();

            if (containsUnits(rebloggedStatus) && isByOtherUser(rebloggedStatus)) {
                return status.reblogged();
            }
        }

        // reply to?
        if (status.inReplyToId().isPresent()) {
            final MastodonStatusId inReplyToStatusId = status.inReplyToId().orElseThrow();
            // get status
            Optional<MastodonStatus> ancestor = executor.copy(this.mastodonClient.getStatusById(inReplyToStatusId))
                    .orTimeout(5, TimeUnit.SECONDS)
                    .handleAsync((Optional<MastodonStatus> result, Throwable error) ->
                            logOrReturnRetrievedTweet(inReplyToStatusId, result, error))
                    .join();
            if (ancestor.isEmpty()) {
                return Optional.empty();
            }

            MastodonStatus replyToStatus = ancestor.orElseThrow();
            if (containsUnits(replyToStatus) && isByOtherUser(replyToStatus)) {
                return Optional.of(replyToStatus);
            }
        }

        return Optional.empty();
    }

    private static Optional<MastodonStatus> logOrReturnRetrievedTweet(
            MastodonStatusId inReplyToStatusId, Optional<MastodonStatus> result, Throwable error) {
        if (error instanceof TimeoutException) {
            return Optional.empty();
        }
        if (error != null) {
            LOG.error("not found, status: " + inReplyToStatusId, error);
            return Optional.empty();
        }
        return result;
    }

    private boolean isByOtherUser(MastodonStatus status) {
        return !status.account()
                .acct()
                .toLowerCase(Locale.ROOT)
                .contains(this.mastodonConfig.getAccountName().toLowerCase(Locale.ROOT));
    }

    private boolean containsUnits(MastodonStatus status) {
        return this.converter.containsUsUnits(status.rawContent());
    }

    @Transactional
    public void upsert(
            final MastodonStatusId id,
            final Instant tweetTime,
            final MastodonStatusId responseId,
            final Instant responseTime) {
        this.repository.get().upsert(id.value(), tweetTime, responseId.value(), responseTime);
    }

    @Transactional
    private Optional<MastodonStatusPdo> findById(final MastodonStatusId id) {
        return this.repository.get().findById(id.value());
    }
}
