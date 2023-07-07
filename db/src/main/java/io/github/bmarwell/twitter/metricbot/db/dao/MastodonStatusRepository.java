package io.github.bmarwell.twitter.metricbot.db.dao;

import io.github.bmarwell.twitter.metricbot.db.pdo.MastodonStatusPdo;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

@Dependent
public class MastodonStatusRepository implements Serializable {

    @PersistenceContext(name = "metricbot-persistence-unit")
    private EntityManager entityManager;

    public Optional<MastodonStatusPdo> findById(String id) {
        @Nullable var pdo = entityManager.find(MastodonStatusPdo.class, id);

        return Optional.ofNullable(pdo);
    }

    public MastodonStatusPdo upsert(
            final String id,
            final Instant tweetTime,
            final @Nullable String botResponseId,
            final @Nullable Instant responseTime) {
        Optional<MastodonStatusPdo> existing =
                Optional.ofNullable(this.entityManager.find(MastodonStatusPdo.class, id));

        if (existing.isPresent()) {
            MastodonStatusPdo pdo = existing.orElseThrow();
            pdo.setBotResponseId(botResponseId);
            pdo.setResponseTime(responseTime);

            this.entityManager.merge(pdo);
            this.entityManager.detach(pdo);

            return pdo;
        }

        MastodonStatusPdo pdo = new MastodonStatusPdo(id, tweetTime);
        pdo.setBotResponseId(botResponseId);
        pdo.setResponseTime(responseTime);

        this.entityManager.merge(pdo);
        this.entityManager.detach(pdo);

        return pdo;
    }
}
