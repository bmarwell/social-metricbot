package io.github.bmarwell.social.metricbot.web.bsky.processing;

import io.github.bmarwell.social.metricbot.bsky.BskyStatus;
import io.github.bmarwell.social.metricbot.db.dao.BskyStatusRepository;
import io.github.bmarwell.social.metricbot.web.GlobalStatusUtil;
import io.github.bmarwell.social.metricbot.web.bsky.event.BskyMentionEvent;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Whenever an event was fired, this class will handle it by filtering it and adding to a queue..
 */
@Dependent
@Default
public class BskyMentionProcessor implements Serializable {
    @Serial
    private static final long serialVersionUID = -4252323010900141178L;

    private static final Logger LOG = LoggerFactory.getLogger(BskyMentionProcessor.class);

    @Inject
    private BskyStatusRepository bskyStatusRepository;

    @Inject
    private UnprocessedBskyStatusQueueHolder unprocessedPostQueueHolder;

    @Transactional
    public void processMastodonStatus(@Observes final BskyMentionEvent mentionEvent) {
        final var status = mentionEvent.bskyStatus();

        if (this.bskyStatusRepository.findById(status.cid()).isPresent()) {
            LOG.trace("Skipping BskyStatus [{}] because it was already replied to.", status.cid());
            return;
        }

        if (this.unprocessedPostQueueHolder.contains(status)) {
            LOG.debug("Skipping BskyStatus [{}] because it will be processed soon.", status.cid());
            return;
        }

        final Instant createdAt = status.createdAt();
        // only reply to mentions in the last 10 minutes
        if (createdAt.isBefore(Instant.now().minusSeconds(60 * 10L))) {
            LOG.info("Skipping BskyStatus [{}] because it is too old: [{}].", status.cid(), createdAt);
            this.bskyStatusRepository.upsert(status.cid(), status.createdAt(), null, Instant.now());

            return;
        }

        if (containsBlockedWord(status)) {
            LOG.debug("Skipping Toot [{}] because it is from a blocked user.", status.cid());
            this.bskyStatusRepository.upsert(status.cid(), status.createdAt(), null, Instant.now());

            return;
        }

        this.unprocessedPostQueueHolder.add(status);
    }

    private boolean containsBlockedWord(final BskyStatus status) {
        return GlobalStatusUtil.containsBlockedWord(
                status.text(), status.author().handle(), status.author().displayName());
    }
}
