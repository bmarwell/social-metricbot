package io.github.bmhm.twitter.metricbot.twitter;

import java.time.Instant;
import java.util.StringJoiner;

import io.github.bmhm.twitter.metricbot.events.MentionEvent;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;

public class MentionListener extends TwitterAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(MentionListener.class);

  private final ApplicationEventPublisher eventPublisher;

  public MentionListener(final ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
    LOG.info("Initializing [{}].", this);
  }

  @Override
  public void gotMentions(final ResponseList<Status> statuses) {
    LOG.info("Found mention: [{}].", statuses.size());

    statuses.stream()
        .filter(status -> status.getCreatedAt().toInstant().isAfter(Instant.now().minusSeconds(60 * 10L)))
        .forEach(this::publishEvent);
  }

  private void publishEvent(final Status status) {
    this.eventPublisher.publishEvent(new MentionEvent(this, status));
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "MentionListener{", "}")
        .add("eventPublisher=" + this.eventPublisher)
        .toString();
  }
}
