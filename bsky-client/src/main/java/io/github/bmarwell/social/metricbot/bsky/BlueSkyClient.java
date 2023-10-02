package io.github.bmarwell.social.metricbot.bsky;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BlueSkyClient extends Serializable, AutoCloseable {

    CompletableFuture<List<BskyStatus>> getRecentMentions();
}
