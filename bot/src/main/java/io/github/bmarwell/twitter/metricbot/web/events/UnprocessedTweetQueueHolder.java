package io.github.bmarwell.twitter.metricbot.web.events;

import com.twitter.clientlib.model.Tweet;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayDeque;

@ApplicationScoped
public class UnprocessedTweetQueueHolder implements Serializable {

    @Serial
    private static final long serialVersionUID = 8189263803128027256L;

    private final ArrayDeque<Tweet> processItems = new ArrayDeque<>();

    public boolean contains(final Tweet foundTweet) {
        return this.processItems.contains(foundTweet);
    }

    public void add(final Tweet foundTweet) {
        this.processItems.add(foundTweet);
    }

    public boolean isEmpty() {
        return this.processItems.isEmpty();
    }

    public Tweet poll() {
        return this.processItems.poll();
    }
}
