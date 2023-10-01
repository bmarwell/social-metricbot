package io.github.bmarwell.twitter.metricbot.web.events;

import com.twitter.clientlib.model.Tweet;
import java.io.Serial;
import java.io.Serializable;

public class TweetProcessRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -5938173264340588621L;
    private final Tweet foundTweet;

    /**
     * Constructs a prototypical Event.
     *
     * @throws IllegalArgumentException if source is null.
     */
    public TweetProcessRequest(final Tweet foundTweet) {
        this.foundTweet = foundTweet;
    }

    public Tweet getFoundTweet() {
        return this.foundTweet;
    }
}
