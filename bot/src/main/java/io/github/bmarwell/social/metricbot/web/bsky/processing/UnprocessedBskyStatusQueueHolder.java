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
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serial;
import java.io.Serializable;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@ApplicationScoped
public class UnprocessedBskyStatusQueueHolder implements Serializable {

    @Serial
    private static final long serialVersionUID = 7176463414871413457L;

    private final Deque<BskyStatus> processItems = new ConcurrentLinkedDeque<>();

    public boolean contains(final BskyStatus status) {
        return processItems.contains(status);
    }

    public void add(final BskyStatus status) {
        if (processItems.contains(status)) {
            return;
        }

        if (processItems.stream().anyMatch(pci -> pci.uri().equals(status.uri()))) {
            return;
        }

        this.processItems.add(status);
    }

    public boolean isEmpty() {
        return processItems.isEmpty();
    }

    public synchronized BskyStatus poll() {
        return this.processItems.poll();
    }
}
