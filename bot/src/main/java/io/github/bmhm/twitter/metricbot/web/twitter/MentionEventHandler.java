package io.github.bmhm.twitter.metricbot.web.twitter;

import io.github.bmhm.twitter.metricbot.common.TwitterConfig;
import io.github.bmhm.twitter.metricbot.web.events.MentionEvent;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;

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
    public void gotMentions(final ResponseList<Status> statuses) {
        // don't consider replies to mentions, tweet must contain the account name EXPLICITELY.
        // don't reply to own replies containing the original unit.
        final Set<Status> newMentions = filterStatusToRespondTo(statuses);

        if (LOG.isInfoEnabled() && newMentions.size() > 0) {
            LOG.info("Found mention: [{}], new: [{}].", statuses.size(), newMentions.size());
        }

        newMentions.forEach(this::publishEvent);
    }

    protected Set<Status> filterStatusToRespondTo(ResponseList<Status> statuses) {
        final Set<Status> newMentions = new HashSet<>();
        for (Status status : statuses) {
            if (!shouldRespondTo(status)) {
                continue;
            }

            newMentions.add(status);
        }

        return Set.copyOf(newMentions);
    }

    protected boolean shouldRespondTo(Status status) {
        if (!status.getCreatedAt().toInstant().isAfter(Instant.now().minusSeconds(60 * 10L))) {
            // not from within last 600s (10 minutes).
            return false;
        }

        if (!status.getText().contains(this.twitterConfig.getAccountName())) {
            return false;
        }

        if (this.twitterConfig.getAccountName().contains(status.getUser().getName())) {
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

    private void publishEvent(final Status status) {
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
