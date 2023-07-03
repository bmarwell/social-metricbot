package io.github.bmarwell.twitter.metricbot.web.mastodon;

import io.github.bmarwell.twitter.metricbot.common.MastodonConfig;
import io.github.bmarwell.twitter.metricbot.mastodon.MastodonStatus;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class MastodonResponseProducer implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(MastodonResponseProducer.class);

    @Resource
    private ManagedScheduledExecutorService scheduler;

    @Inject
    private UnprocessedMastodonStatusQueueHolder unprocessedMastodonStatusQueueHolder;

    @Inject
    private MastodonConfig mastodonConfig;

    @Inject
    private Event<MastodonProcessRequest> processEvent;

    private final Object emitterLock = new Object();

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        ScheduledFuture<?> scheduledFuture = this.scheduler.scheduleAtFixedRate(
                this::emitMention,
                this.mastodonConfig.getTweetFinderInitialDelay().getSeconds(),
                // delay here may be short b/c response are usually rare
                2L,
                TimeUnit.SECONDS);

        if (scheduledFuture.isCancelled()) {
            throw new IllegalStateException("Scheduler was canceled: " + scheduledFuture);
        }
    }

    protected void emitMention() {
        synchronized (this.emitterLock) {
            if (this.unprocessedMastodonStatusQueueHolder.isEmpty()) {
                LOG.trace("No tweet to reply to.");
                return;
            }

            final MastodonStatus foundStatus = this.unprocessedMastodonStatusQueueHolder.poll();
            LOG.info(
                    "Emitting event for toot: [{}]/[{}].",
                    foundStatus.id(),
                    foundStatus.rawContent().replaceAll("\n", "\\\\n"));

            this.processEvent.fire(new MastodonProcessRequest(foundStatus));
        }
    }
}
