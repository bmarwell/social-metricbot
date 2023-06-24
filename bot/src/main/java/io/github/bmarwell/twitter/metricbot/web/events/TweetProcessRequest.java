package io.github.bmarwell.twitter.metricbot.web.events;

import twitter4j.Status;

public class TweetProcessRequest {

    private static final long serialVersionUID = -5938173264340588621L;
    private final Status foundTweet;

    /**
     * Constructs a prototypical Event.
     *
     * @throws IllegalArgumentException if source is null.
     */
    public TweetProcessRequest(final Status foundTweet) {
        this.foundTweet = foundTweet;
    }

    public Status getFoundTweet() {
        return this.foundTweet;
    }
}
