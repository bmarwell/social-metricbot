package io.github.bmarwell.twitter.metricbot.web.listener;

import io.github.bmarwell.twitter.metricbot.common.TwitterConfig;
import io.github.bmarwell.twitter.metricbot.web.events.TweetProcessRequest;
import io.github.bmarwell.twitter.metricbot.web.events.UnprocessedTweetQueueHolder;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;

@WebListener
public class TweetResponseListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(TweetMentionListener.class);

    @Resource
    private ManagedScheduledExecutorService scheduler;

    @Inject
    private Event<TweetProcessRequest> processEvent;

    @Inject
    private TwitterConfig twitterConfig;

    @Inject
    private UnprocessedTweetQueueHolder unprocessedTweetQueueHolder;

    private final Object emitterLock = new Object();

    public TweetResponseListener() {
        // injection constructor
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        this.scheduler.scheduleAtFixedRate(
                this::emitMention,
                this.twitterConfig.getTweetFinderInitialDelay(),
                // delay here may be short b/c response are usually rare
                2L,
                TimeUnit.SECONDS);
    }

    /**
     * Only emit a new tweet once every 5 seconds.
     */
    protected void emitMention() {
        synchronized (this.emitterLock) {
            if (this.unprocessedTweetQueueHolder.isEmpty()) {
                LOG.trace("No tweet to reply to.");
                return;
            }

            final Status foundTweet = this.unprocessedTweetQueueHolder.poll();
            LOG.info(
                    "Emitting event for tweet: [{}]/[{}].",
                    foundTweet.getId(),
                    foundTweet.getText().replaceAll("\n", "\\\\n"));

            this.processEvent.fire(new TweetProcessRequest(foundTweet));
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TweetResponseListener.class.getSimpleName() + "[", "]")
                .add("scheduler=" + this.scheduler)
                .add("processEvent=" + this.processEvent)
                .add("twitterConfig=" + this.twitterConfig)
                .add("unprocessedTweetQueueHolder=" + this.unprocessedTweetQueueHolder)
                .add("emitterLock=" + this.emitterLock)
                .toString();
    }
}
