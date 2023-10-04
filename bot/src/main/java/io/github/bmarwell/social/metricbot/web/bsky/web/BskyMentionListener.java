package io.github.bmarwell.social.metricbot.web.bsky.web;

import io.github.bmarwell.social.metricbot.bsky.BlueSkyClient;
import io.github.bmarwell.social.metricbot.bsky.BskyStatus;
import io.github.bmarwell.social.metricbot.web.bsky.api.BskyProducer;
import io.github.bmarwell.social.metricbot.web.bsky.event.BskyMentionEvent;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class BskyMentionListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(BskyMentionListener.class);

    @Resource
    private ManagedScheduledExecutorService scheduler;

    @Resource
    private ManagedExecutorService executor;

    @Inject
    private BskyProducer bskyProducer;

    @Inject
    private Event<BskyMentionEvent> mentionEvent;

    private BlueSkyClient bsky;

    public BskyMentionListener() {
        // for injection
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        ServletContextListener.super.contextInitialized(sce);
        LOG.info("init: [{}].", this);

        this.bsky = this.bskyProducer.produceBlueSky();

        // set up scheduler
        final ScheduledFuture<?> scheduledFuture =
                this.scheduler.scheduleWithFixedDelay(this::retrieveMentions, 10, 10, TimeUnit.SECONDS);

        if (scheduledFuture.isCancelled()) {
            throw new IllegalStateException("MentionListener did not start!" + scheduledFuture);
        }
    }

    protected void retrieveMentions() {
        executor.copy(this.bsky.getRecentMentions())
                // no method returning void?
                .handle((final List<BskyStatus> result, final Throwable error) -> {
                    if (error != null) {
                        LOG.error("could not retrieve mentions: [" + error.getMessage() + "].", error);
                        return null;
                    }

                    for (final BskyStatus recentMention : result) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("[BSKY] Found mention: " + recentMention);
                        }
                        mentionEvent.fire(new BskyMentionEvent(recentMention));
                    }

                    return null;
                });
    }
}
