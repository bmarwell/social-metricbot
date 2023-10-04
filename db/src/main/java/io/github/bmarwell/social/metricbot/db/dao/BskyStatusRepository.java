package io.github.bmarwell.social.metricbot.db.dao;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.bmarwell.social.metricbot.db.pdo.BskyStatusPdo;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

@Dependent
@Default
public class BskyStatusRepository implements Serializable {

    @Serial
    private static final long serialVersionUID = 6270206030149625623L;

    @PersistenceContext(name = "metricbot-persistence-unit")
    private EntityManager entityManager;

    public Optional<BskyStatusPdo> findByAtUri(final URI postAtUri) {
        @Nullable final var pdo = entityManager.find(BskyStatusPdo.class, postAtUri);

        return Optional.ofNullable(pdo);
    }

    @CanIgnoreReturnValue
    public BskyStatusPdo upsert(
            final URI bskyStatusAtUri,
            final Instant statusCreatedAt,
            final @Nullable URI botResponseAtUri,
            final @Nullable Instant repliedAt) {
        final Optional<BskyStatusPdo> existing =
                Optional.ofNullable(this.entityManager.find(BskyStatusPdo.class, bskyStatusAtUri));

        if (existing.isPresent()) {
            final BskyStatusPdo pdo = existing.orElseThrow();
            pdo.setBotResponseAtUri(botResponseAtUri);
            pdo.setResponseTime(repliedAt);

            this.entityManager.merge(pdo);
            this.entityManager.detach(pdo);

            return pdo;
        }

        final BskyStatusPdo pdo = new BskyStatusPdo(bskyStatusAtUri, statusCreatedAt);
        pdo.setBotResponseAtUri(botResponseAtUri);
        pdo.setResponseTime(repliedAt);

        this.entityManager.merge(pdo);
        this.entityManager.detach(pdo);

        return pdo;
    }
}
