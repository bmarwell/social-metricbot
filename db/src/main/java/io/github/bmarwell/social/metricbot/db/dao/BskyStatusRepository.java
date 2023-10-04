package io.github.bmarwell.social.metricbot.db.dao;

import io.github.bmarwell.social.metricbot.db.pdo.BskyStatusPdo;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

@Dependent
@Default
public class BskyStatusRepository implements Serializable {

    @Serial
    private static final long serialVersionUID = 6270206030149625623L;

    @PersistenceContext(name = "metricbot-persistence-unit")
    private EntityManager entityManager;

    public Optional<BskyStatusPdo> findById(final String cid) {
        @Nullable var pdo = entityManager.find(BskyStatusPdo.class, cid);

        return Optional.ofNullable(pdo);
    }

    public void upsert(
            final String bskyStatusCid,
            final Instant statusCreatedAt,
            final @Nullable String botResponseCid,
            final @Nullable Instant repliedAt) {
        // TODO: implement
        throw new UnsupportedOperationException(
                "not yet implemented: [io.github.bmarwell.social.metricbot.db.dao.BskyStatusRepository::upsert].");
    }
}
