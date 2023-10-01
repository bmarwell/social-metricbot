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
