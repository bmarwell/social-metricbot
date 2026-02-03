/*
 * Copyright 2023 The social-metricbot contributors
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
package io.github.bmarwell.social.metricbot.web.bsky.web;

import io.github.bmarwell.social.metricbot.common.BlueSkyBotConfig;
import io.github.bmarwell.social.metricbot.web.bsky.processing.BskyProcessRequest;
import io.github.bmarwell.social.metricbot.web.bsky.processing.UnprocessedBskyStatusQueueHolder;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will poll from the Queue and fire an event.
 */
@WebListener
public class BskyResponseProducer implements ServletContextListener, Serializable {

    @Serial
    private static final long serialVersionUID = -4508296428501306541L;

    private static final Logger LOG = LoggerFactory.getLogger(BskyResponseProducer.class);

    @Resource
    private ManagedScheduledExecutorService scheduler;

    @Resource
    private ManagedExecutorService executor;

    @Inject
    private UnprocessedBskyStatusQueueHolder unprocessedBskyStatusQueueHolder;

    @Inject
    private BlueSkyBotConfig bskyConfig;

    @Inject
    private Event<BskyProcessRequest> processEvent;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        final var scheduledFuture = this.scheduler.scheduleAtFixedRate(
                this::emitMention,
                this.bskyConfig.getPostFinderInitialDelay().getSeconds(),
                // delay here may be short b/c response are usually rare
                2L,
                TimeUnit.SECONDS);

        if (scheduledFuture.isCancelled()) {
            throw new IllegalStateException("Scheduler was canceled: " + scheduledFuture);
        }
    }

    private void emitMention() {
        if (this.unprocessedBskyStatusQueueHolder.isEmpty()) {
            LOG.trace("No BskyStatus to reply to.");
            return;
        }

        final var bskyStatus = this.unprocessedBskyStatusQueueHolder.poll();
        LOG.debug(
                "Emitting event for BskyStatus: [{}]/[{}].",
                bskyStatus.uri(),
                bskyStatus.text().replaceAll("\n", "\\\\n"));
        this.processEvent.fireAsync(
                new BskyProcessRequest(bskyStatus),
                NotificationOptions.builder().setExecutor(executor).build());
    }
}
