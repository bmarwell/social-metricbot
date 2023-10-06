package io.github.bmarwell.social.metricbot.web.mastodon;

import io.github.bmarwell.social.metricbot.db.dao.MastodonStatusRepository;
import io.github.bmarwell.social.metricbot.mastodon.MastodonStatus;
import io.github.bmarwell.social.metricbot.web.GlobalStatusUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MastodonStatusProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MastodonStatusProcessor.class);

    @Inject
    private MastodonStatusRepository mastodonStatusRepository;

    @Inject
    private UnprocessedMastodonStatusQueueHolder unprocessedTweetQueueHolder;

    @Transactional
    public void processMastodonStatus(@Observes MastodonMentionEvent mentionEvent) {
        final MastodonStatus status = mentionEvent.mastodonStatus();

        if (this.mastodonStatusRepository.findById(status.id().value()).isPresent()) {
            LOG.trace("Skipping Toot [{}] because it was already replied to.", status.id());
            return;
        }

        if (this.unprocessedTweetQueueHolder.contains(status)) {
            LOG.debug("Skipping Toot [{}] because it will be processed soon.", status.id());
            return;
        }

        final Instant createdAt = status.createdAt();
        // only reply to mentions in the last 10 minutes
        if (createdAt.isBefore(Instant.now().minusSeconds(60 * 10L))) {
            LOG.info("Skipping Toot [{}] because it is too old: [{}].", status.id(), createdAt);
            this.mastodonStatusRepository.upsert(status.id().value(), status.createdAt(), null, Instant.now());

            return;
        }

        if (containsBlockedWord(status)) {
            LOG.debug("Skipping Toot [{}] because it is from a blocked user.", status.id());
            this.mastodonStatusRepository.upsert(status.id().value(), status.createdAt(), null, Instant.now());

            return;
        }

        this.unprocessedTweetQueueHolder.add(status);
    }

    protected boolean containsBlockedWord(final MastodonStatus mastodonStatus) {
        return GlobalStatusUtil.containsBlockedWord(
                mastodonStatus.rawContent(),
                mastodonStatus.account().acct(),
                mastodonStatus.account().username());
    }
}
