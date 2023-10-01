package io.github.bmarwell.twitter.metricbot.web.events;

import com.twitter.clientlib.model.Tweet;
import java.io.Serial;
import java.io.Serializable;
import java.util.StringJoiner;

public class MentionEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = -293752304434586050L;
    private final Tweet foundTweet;

    /**
     * Constructs a prototypical Event.
     *
     * @throws IllegalArgumentException if source is null.
     */
    public MentionEvent(final Tweet foundTweet) {
        this.foundTweet = foundTweet;
    }

    public Tweet getFoundTweet() {
        return this.foundTweet;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MentionEvent.class.getSimpleName() + "[", "]")
                .add("foundTweet=" + this.foundTweet.toString().replaceAll("\n", "\\\\n"))
                .toString();
    }
}
