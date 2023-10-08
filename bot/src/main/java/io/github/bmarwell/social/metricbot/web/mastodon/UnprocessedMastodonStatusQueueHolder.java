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
package io.github.bmarwell.social.metricbot.web.mastodon;

import io.github.bmarwell.social.metricbot.mastodon.MastodonStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.ArrayDeque;

@ApplicationScoped
public class UnprocessedMastodonStatusQueueHolder implements Serializable {

    private final ArrayDeque<MastodonStatus> processItems = new ArrayDeque<>();

    public boolean contains(MastodonStatus foundStatus) {
        return processItems.contains(foundStatus);
    }

    public void add(final MastodonStatus mastodonStatus) {
        this.processItems.add(mastodonStatus);
    }

    public boolean isEmpty() {
        return this.processItems.isEmpty();
    }

    public MastodonStatus poll() {
        return this.processItems.poll();
    }
}
