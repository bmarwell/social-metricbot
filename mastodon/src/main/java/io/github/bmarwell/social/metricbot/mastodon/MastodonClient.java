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
package io.github.bmarwell.social.metricbot.mastodon;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface MastodonClient extends AutoCloseable {

    CompletionStage<List<MastodonStatus>> getRecentMentions();

    CompletableFuture<Optional<MastodonStatus>> getStatusById(MastodonStatusId id);

    CompletableFuture<Optional<MastodonStatus>> postStatus(MastodonTextStatusDraft statusToSend);
}
