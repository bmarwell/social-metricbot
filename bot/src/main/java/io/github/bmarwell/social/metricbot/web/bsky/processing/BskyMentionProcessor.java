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
package io.github.bmarwell.social.metricbot.web.bsky.processing;

import io.github.bmarwell.social.metricbot.bsky.BskyStatus;
import io.github.bmarwell.social.metricbot.common.BlueSkyBotConfig;
import io.github.bmarwell.social.metricbot.db.dao.BskyStatusRepository;
import io.github.bmarwell.social.metricbot.web.GlobalStatusUtil;
import io.github.bmarwell.social.metricbot.web.bsky.event.BskyMentionEvent;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Whenever an event was fired, this class will handle it by filtering it and adding to a queue..
 */
@Dependent
@Default
public class BskyMentionProcessor implements Serializable {
    @Serial
    private static final long serialVersionUID = -4252323010900141178L;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private BskyStatusRepository bskyStatusRepository;

    @Inject
    private UnprocessedBskyStatusQueueHolder unprocessedPostQueueHolder;

    @Inject
    private BlueSkyBotConfig blueSkyBotConfig;

    @Transactional
    public void processMastodonStatus(@Observes final BskyMentionEvent mentionEvent) {
        final var status = mentionEvent.bskyStatus();

        if (this.bskyStatusRepository.findByAtUri(status.uri()).isPresent()) {
            log.trace("Skipping BskyStatus [{}] because it was already replied to.", status.uri());
            return;
        }

        if (this.unprocessedPostQueueHolder.contains(status)) {
            log.debug("Skipping BskyStatus [{}] because it will be processed soon.", status.uri());
            return;
        }

        final Instant createdAt = status.createdAt();
        // only reply to mentions in the last 10 minutes
        if (skipOld() && createdAt.isBefore(Instant.now().minusSeconds(60 * 10L))) {
            log.info("Skipping BskyStatus [{}] because it is too old: [{}].", status.uri(), createdAt);
            this.bskyStatusRepository.upsert(status.uri(), status.createdAt(), null, Instant.now());

            return;
        }

        if (containsBlockedWord(status)) {
            log.debug("Skipping Toot [{}] because it is from a blocked user.", status.uri());
            this.bskyStatusRepository.upsert(status.uri(), status.createdAt(), null, Instant.now());

            return;
        }

        this.unprocessedPostQueueHolder.add(status);
    }

    private boolean skipOld() {
        return blueSkyBotConfig.isSkipOldPosts();
    }

    private boolean containsBlockedWord(final BskyStatus status) {
        return GlobalStatusUtil.containsBlockedWord(
                status.text(), status.author().handle(), status.author().displayName());
    }
}
