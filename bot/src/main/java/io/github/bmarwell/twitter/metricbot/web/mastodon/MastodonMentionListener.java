package io.github.bmarwell.twitter.metricbot.web.mastodon;

import io.github.bmarwell.twitter.metricbot.mastodon.MastodonClient;
import io.github.bmarwell.twitter.metricbot.mastodon.MastodonStatus;
import io.github.bmarwell.twitter.metricbot.web.factory.MastodonProducer;
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
public class MastodonMentionListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(MastodonMentionListener.class);

    @Resource
    private ManagedScheduledExecutorService scheduler;

    @Resource
    private ManagedExecutorService executor;

    @Inject
    private MastodonProducer mastodonProducer;

    @Inject
    private Event<MastodonMentionEvent> mentionEvent;

    private MastodonClient mastodon;

    public MastodonMentionListener() {
        // injection constructor
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContextListener.super.contextInitialized(sce);
        LOG.info("init: [{}].", this);
        this.mastodon = this.mastodonProducer.produceMastodon();

        // set up scheduler
        ScheduledFuture<?> scheduledFuture =
                this.scheduler.scheduleWithFixedDelay(this::retrieveMentions, 10, 10, TimeUnit.SECONDS);

        if (scheduledFuture.isCancelled()) {
            throw new IllegalStateException("MentionListener did not start!" + scheduledFuture);
        }
    }

    protected void retrieveMentions() {
        executor.copy(this.mastodon.getRecentMentions())
                // no method returning void?
                .handle((List<MastodonStatus> result, Throwable error) -> {
                    if (error != null) {
                        LOG.error("could not retrieve mentions: [" + error.getMessage() + "].", error);
                        return null;
                    }

                    for (MastodonStatus recentMention : result) {
                        mentionEvent.fire(new MastodonMentionEvent(recentMention));
                    }

                    return null;
                });
    }
}
