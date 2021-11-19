package io.github.bmhm.twitter.metricbot.web.twitter;

import io.github.bmhm.twitter.metricbot.common.TwitterConfig;
import io.github.bmhm.twitter.metricbot.web.events.MentionEvent;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

@Dependent
public class MentionEventHandler extends TwitterAdapter implements Serializable {

  private static final long serialVersionUID = -7892279525267720394L;

  private static final Logger LOG = LoggerFactory.getLogger(MentionEventHandler.class);

  @Inject
  private Event<MentionEvent> mentionEvent;

  @Inject
  private TwitterConfig twitterConfig;

  public MentionEventHandler() {

  }

  public MentionEventHandler(final Event<MentionEvent> mentionEvent) {
    this.mentionEvent = mentionEvent;
    LOG.info("Initializing [{}].", this);
  }

  @Override
  public void gotMentions(final ResponseList<Status> statuses) {
    final Set<Status> newMentions = statuses.stream()
        .filter(status -> status.getCreatedAt().toInstant()
            .isAfter(Instant.now().minusSeconds(60 * 10L)))
        // don't consider replies to mentions, tweet must contain the account name EXPLICITELY.
        .filter(status -> status.getText().contains(this.twitterConfig.getAccountName()))
        // don't reply to own replies containing the original unit.
        .filter(status -> !this.twitterConfig.getAccountName().contains(status.getUser().getName()))
        .collect(Collectors.toSet());

    if (LOG.isInfoEnabled() && newMentions.size() > 0) {
      LOG.info("Found mention: [{}], new: [{}].", statuses.size(), newMentions.size());
    }

    newMentions.forEach(this::publishEvent);
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

  @Override
  public String toString() {
    return new StringJoiner(", ", "MentionEventHandler{", "}")
        .add("mentionEvent=" + this.mentionEvent)
        .toString();
  }
}
