package io.github.bmhm.twitter.metricbot.events;

import io.micronaut.context.event.ApplicationEvent;
import twitter4j.Status;

public class TweetProcessRequest extends ApplicationEvent {

  private static final long serialVersionUID = -5938173264340588621L;
  private final Status foundTweet;

  /**
   * Constructs a prototypical Event.
   *
   * @param source
   *     The object on which the Event initially occurred.
   * @throws IllegalArgumentException
   *     if source is null.
   */
  public TweetProcessRequest(final Object source, final Status foundTweet) {
    super(source);
    this.foundTweet = foundTweet;
  }

  public Status getFoundTweet() {
    return this.foundTweet;
  }
}
