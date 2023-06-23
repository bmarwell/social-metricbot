package io.github.bmhm.twitter.metricbot.web.events;

import java.util.StringJoiner;
import twitter4j.Status;

public class MentionEvent {

    private static final long serialVersionUID = -293752304434586050L;
    private final Status foundTweet;

    /**
     * Constructs a prototypical Event.
     *
     * @throws IllegalArgumentException if source is null.
     */
    public MentionEvent(final Status foundTweet) {
        this.foundTweet = foundTweet;
    }

    public Status getFoundTweet() {
        return this.foundTweet;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MentionEvent.class.getSimpleName() + "[", "]")
                .add("foundTweet=" + this.foundTweet)
                .toString();
    }
}
