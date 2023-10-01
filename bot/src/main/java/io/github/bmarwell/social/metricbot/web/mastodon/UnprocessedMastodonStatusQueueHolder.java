package io.github.bmarwell.social.metricbot.web.mastodon;

import io.github.bmarwell.social.metricbot.mastodon.MastodonStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.ArrayDeque;

@ApplicationScoped
public class UnprocessedMastodonStatusQueueHolder implements Serializable {

    private final ArrayDeque<MastodonStatus> processItems = new ArrayDeque<>();

    public boolean contains(MastodonStatus foundStatus) {
        return processItems.contains(foundStatus);
    }

    public void add(final MastodonStatus mastodonStatus) {
        this.processItems.add(mastodonStatus);
    }

    public boolean isEmpty() {
        return this.processItems.isEmpty();
    }

    public MastodonStatus poll() {
        return this.processItems.poll();
    }
}
