package io.github.bmarwell.twitter.metricbot.web.mastodon;

import static java.util.Arrays.asList;

import io.github.bmarwell.twitter.metricbot.db.dao.MastodonStatusRepository;
import io.github.bmarwell.twitter.metricbot.mastodon.MastodonStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MastodonStatusProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MastodonStatusProcessor.class);

    private static final List<String> ACCOUNT_NAME_WORD_BLACKLIST =
            asList("Boutique", "Crazy I Buy", "weather", "Supplements", "DealsSupply");

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

    protected boolean containsBlockedWord(final MastodonStatus tweet) {
        final String userName = tweet.account().acct();

        return ACCOUNT_NAME_WORD_BLACKLIST.stream()
                // none of the blacklisted words should be contained in the full account name
                .anyMatch(blacklisted ->
                        userName.toLowerCase(Locale.ENGLISH).contains(blacklisted.toLowerCase(Locale.ENGLISH)));
    }
}
