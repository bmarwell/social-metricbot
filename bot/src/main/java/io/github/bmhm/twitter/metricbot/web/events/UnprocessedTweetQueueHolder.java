package io.github.bmhm.twitter.metricbot.web.events;

import twitter4j.Status;

import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.ArrayDeque;

@ApplicationScoped
public class UnprocessedTweetQueueHolder implements Serializable {

  private static final long serialVersionUID = 8189263803128027256L;

  private final ArrayDeque<Status> processItems = new ArrayDeque<>();

  public boolean contains(final Status foundTweet) {
    return this.processItems.contains(foundTweet);
  }

  public void add(final Status foundTweet) {
    this.processItems.add(foundTweet);
  }

  public boolean isEmpty() {
    return this.processItems.isEmpty();
  }

  public Status poll() {
    return this.processItems.poll();
  }
}
