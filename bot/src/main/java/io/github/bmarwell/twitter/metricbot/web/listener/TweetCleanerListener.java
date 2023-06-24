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
import io.github.bmarwell.twitter.metricbot.db.dao.TweetRepository;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.transaction.Transactional;

@WebListener
public class TweetCleanerListener implements ServletContextListener {

    @Resource
    private ManagedScheduledExecutorService scheduler;

    /**
     * recent replies.
     */
    @Inject
    private TweetRepository tweetRepository;

    @Inject
    private TwitterConfig twitterConfig;

    public TweetCleanerListener() {
        // injection
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        this.scheduler.scheduleWithFixedDelay(
                this::removeOldTweets, this.twitterConfig.getTweetFinderInitialDelay(), 10 * 60, TimeUnit.SECONDS);
    }

    @Transactional
    public void removeOldTweets() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime oneWeekAgo = now.minusDays(7L);
        final Instant deleteBefore = oneWeekAgo.toInstant();

        this.tweetRepository.deleteByTweetTimeBefore(deleteBefore);
    }
}
