/*
 * Copyright 2021-2023 The social-metricbot contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bmarwell.social.metricbot.web.mastodon;

import io.github.bmarwell.social.metricbot.common.MastodonConfig;
import io.github.bmarwell.social.metricbot.mastodon.MastodonStatus;
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
