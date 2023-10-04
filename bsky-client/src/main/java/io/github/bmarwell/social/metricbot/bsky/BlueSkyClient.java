package io.github.bmarwell.social.metricbot.bsky;

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
}
