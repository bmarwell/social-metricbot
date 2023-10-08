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
package io.github.bmarwell.social.metricbot.bsky;

import io.github.bmarwell.social.metricbot.bsky.json.BskyResponseDraft;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BlueSkyClient extends Serializable, AutoCloseable {

    CompletableFuture<List<BskyStatus>> getRecentMentions();

    Optional<BskyStatus> getRepliedToPost(BskyStatus status);

    Optional<BskyStatus> getSinglePost(URI replyTo);

    /**
     * Convenience method to check whether the post author (identified by the handle without leading {@code @})
     * is the same as the configured API handle.
     *
     * @param status the status to check the author for.
     * @return {@code true} if this status was not posted by the API user.
     */
    boolean isByOtherUser(BskyStatus status);

    /**
     * If this status quotes another status (with the embed feature), get the quoted (reposted) status.
     *
     * @param status the current status with ({@link BskyStatus#isQuoting()} {@code == true}.
     * @return an optional status if found.
     */
    Optional<BskyStatus> getRepostedStatus(BskyStatus status);

    Optional<BskyStatus> sendReply(BskyResponseDraft statusDraft);

    String getHandle();

    URI getStatusUri(BskyStatus bskyStatus);
}
