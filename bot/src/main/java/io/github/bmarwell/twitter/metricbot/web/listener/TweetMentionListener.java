/*
 *  Copyright 2018 The twittermetricbot contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.bmarwell.twitter.metricbot.web.listener;

import io.github.bmarwell.twitter.metricbot.common.TwitterConfig;
import io.github.bmarwell.twitter.metricbot.web.factory.TwitterProducer;
import io.github.bmarwell.twitter.metricbot.web.twitter.MentionEventHandler;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.AsyncTwitter;

@WebListener
public class TweetMentionListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(TweetMentionListener.class);

    @Resource
    private ManagedScheduledExecutorService scheduler;

    @Inject
    private TwitterProducer twitterProducer;

    @Inject
    private TwitterConfig twitterConfig;

    @Inject
    private MentionEventHandler mentionEventHandler;

    private AsyncTwitter asyncTwitter;

    public TweetMentionListener() {
        // injection constructor
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        ServletContextListener.super.contextInitialized(sce);
        LOG.info("init: [{}].", this);
        this.asyncTwitter = this.twitterProducer.getAsyncTwitter().getInstance();
        this.asyncTwitter.addListener(this.mentionEventHandler);

        // set up scheduler
        this.scheduler.scheduleWithFixedDelay(
                this::retrieveMentions,
                this.twitterConfig.getTweetFinderInitialDelay(),
                this.twitterConfig.getTweetFinderRetrieveRate(),
                TimeUnit.SECONDS);
    }

    protected void retrieveMentions() {
        this.asyncTwitter.getMentions();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TweetMentionListener.class.getSimpleName() + "[", "]")
                .add("scheduler=" + this.scheduler)
                .add("twitterProducer=" + this.twitterProducer)
                .add("twitterConfig=" + this.twitterConfig)
                .add("mentionEventHandler=" + this.mentionEventHandler)
                .add("asyncTwitter=" + this.asyncTwitter)
                .toString();
    }
}
