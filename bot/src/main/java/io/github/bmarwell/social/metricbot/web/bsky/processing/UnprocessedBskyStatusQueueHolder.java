package io.github.bmarwell.social.metricbot.web.bsky.processing;

import io.github.bmarwell.social.metricbot.bsky.BskyStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serial;
import java.io.Serializable;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@ApplicationScoped
public class UnprocessedBskyStatusQueueHolder implements Serializable {

    @Serial
    private static final long serialVersionUID = 7176463414871413457L;

    private final Deque<BskyStatus> processItems = new ConcurrentLinkedDeque<>();

    public boolean contains(final BskyStatus status) {
        return processItems.contains(status);
    }

    public void add(final BskyStatus status) {
        if (processItems.contains(status)) {
            return;
        }

        if (processItems.stream().anyMatch(pci -> pci.uri().equals(status.uri()))) {
            return;
        }

        this.processItems.add(status);
    }

    public boolean isEmpty() {
        return processItems.isEmpty();
    }

    public synchronized BskyStatus poll() {
        return this.processItems.poll();
    }
}
