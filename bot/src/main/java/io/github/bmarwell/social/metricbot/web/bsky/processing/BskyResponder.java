package io.github.bmarwell.social.metricbot.web.bsky.processing;

import io.github.bmarwell.social.metricbot.bsky.BlueSkyClient;
import io.github.bmarwell.social.metricbot.bsky.BskyStatus;
import io.github.bmarwell.social.metricbot.conversion.UsConversion;
import io.github.bmarwell.social.metricbot.db.dao.BskyStatusRepository;
import io.github.bmarwell.social.metricbot.db.pdo.BskyStatusPdo;
import io.github.bmarwell.social.metricbot.web.AbstractResponder;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
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

    public void onMastodonStatusFound(@Observes final BskyProcessRequest event) {
        LOG.info("Checking response to event [{}].", event);
        final BskyStatus status = event.status();

        final Optional<BskyStatusPdo> alreadyRespondedToMention =
                this.self.get().findById(status.uri());
        if (alreadyRespondedToMention.isPresent()) {
            final BskyStatusPdo tweetPdo = alreadyRespondedToMention.orElseThrow();
            LOG.debug("Already responded: [{}]", tweetPdo);

            return;
        }

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

        // TODO: doRespond(status, statusWithUnits);
    }

    private Optional<BskyStatus> getStatusWithUnits(final BskyStatus status) {
        // tweet itself?
        if (containsUnits(status) && this.bskyClient.isByOtherUser(status)) {
            LOG.info("Post itself contains units.");
            return Optional.of(status);
        }

        // boosted? Same as quoted and retweeted/reblogged for Mastodon.
        if (status.isQuoting()) {
            final BskyStatus rebloggedStatus =
                    this.bskyClient.getRepostedStatus(status).orElseThrow();

            if (containsUnits(rebloggedStatus) && this.bskyClient.isByOtherUser(rebloggedStatus)) {
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
}
