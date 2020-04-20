package io.github.bmhm.twitter.metricbot.events;

import io.micronaut.context.event.ApplicationEvent;
import twitter4j.Status;

public class MentionEvent extends ApplicationEvent {

  private static final long serialVersionUID = -293752304434586050L;
  private final Status foundTweet;

  /**
   * Constructs a prototypical Event.
   *
   * @param source
   *     The object on which the Event initially occurred.
   * @throws IllegalArgumentException
   *     if source is null.
   */
  public MentionEvent(final Object source, final Status foundTweet) {
    super(source);
    this.foundTweet = foundTweet;
  }

  public Status getFoundTweet() {
    return this.foundTweet;
  }
}
