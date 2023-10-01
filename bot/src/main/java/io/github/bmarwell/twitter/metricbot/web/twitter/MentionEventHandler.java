package io.github.bmarwell.twitter.metricbot.web.twitter;

import com.twitter.clientlib.model.Tweet;
import io.github.bmarwell.twitter.metricbot.common.TwitterConfig;
import io.github.bmarwell.twitter.metricbot.web.events.MentionEvent;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class MentionEventHandler extends TwitterAdapter implements Serializable {

    private static final long serialVersionUID = -7892279525267720394L;

    private static final Logger LOG = LoggerFactory.getLogger(MentionEventHandler.class);

    @Inject
    private Event<MentionEvent> mentionEvent;

    @Inject
    private TwitterConfig twitterConfig;

    public MentionEventHandler() {}

    public MentionEventHandler(final Event<MentionEvent> mentionEvent) {
        this.mentionEvent = mentionEvent;
        LOG.info("Initializing [{}].", this);
    }

    @Override
    public void gotMentions(final ResponseList<Tweet> statuses) {
        // don't consider replies to mentions, tweet must contain the account name EXPLICITELY.
        // don't reply to own replies containing the original unit.
        final Set<Tweet> newMentions = filterStatusToRespondTo(statuses);

        if (LOG.isDebugEnabled() && newMentions.size() > 0) {
            LOG.debug("Found mention: [{}], new: [{}].", statuses.size(), newMentions.size());
        }

        newMentions.forEach(this::publishEvent);
    }

    protected Set<Tweet> filterStatusToRespondTo(ResponseList<Tweet> statuses) {
        final Set<Tweet> newMentions = new HashSet<>();
        for (Tweet status : statuses) {
            if (!shouldRespondTo(status)) {
                continue;
            }

            newMentions.add(status);
        }

        return Set.copyOf(newMentions);
    }

    protected boolean shouldRespondTo(Tweet status) {
        if (status.getUser().getScreenName().equalsIgnoreCase(this.twitterConfig.getAccountName())) {
            return false;
        }

        if (!status.getCreatedAt().toInstant().isAfter(Instant.now().minusSeconds(60 * 10L))) {
            // not from within last 600s (10 minutes).
            return false;
        }

        if (!status.getText().contains(this.twitterConfig.getAccountName())) {
            return false;
        }

        if (this.twitterConfig.getAccountName().contains(status.getUser().getScreenName())) {
            return false;
        }

        return status.getText().toLowerCase(Locale.ROOT).contains("please");
    }

    @Override
    public void gotRateLimitStatus(final Map<String, RateLimitStatus> rateLimitStatus) {
        LOG.info("Rate limit status: [{}].", rateLimitStatus);
    }

    @Override
    public void onException(final TwitterException te, final TwitterMethod method) {
        LOG.error("Problem executing [{}].", method, new IllegalStateException(te));
    }

    private void publishEvent(final Tweet status) {
        this.mentionEvent.fire(new MentionEvent(status));
    }

    public void setTwitterConfig(TwitterConfig twitterConfig) {
        this.twitterConfig = twitterConfig;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MentionEventHandler.class.getSimpleName() + "[", "]")
                .add("mentionEvent=" + mentionEvent)
                .add("twitterConfig=" + twitterConfig)
                .toString();
    }
}
