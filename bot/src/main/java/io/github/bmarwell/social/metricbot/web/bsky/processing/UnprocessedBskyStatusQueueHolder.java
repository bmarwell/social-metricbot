package io.github.bmarwell.social.metricbot.web.bsky.processing;

import io.github.bmarwell.social.metricbot.bsky.BskyStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayDeque;

@ApplicationScoped
public class UnprocessedBskyStatusQueueHolder implements Serializable {

    @Serial
    private static final long serialVersionUID = 7176463414871413457L;

    private final ArrayDeque<BskyStatus> processItems = new ArrayDeque<>();

    public boolean contains(final BskyStatus status) {
        return processItems.contains(status);
    }

    public void add(final BskyStatus status) {
        if (processItems.contains(status)) {
            return;
        }

        this.processItems.add(status);
    }
}
