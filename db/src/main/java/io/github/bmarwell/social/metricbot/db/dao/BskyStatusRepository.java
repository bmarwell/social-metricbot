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
