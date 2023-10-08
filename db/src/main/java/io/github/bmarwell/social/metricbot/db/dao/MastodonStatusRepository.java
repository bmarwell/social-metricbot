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

import io.github.bmarwell.social.metricbot.db.pdo.MastodonStatusPdo;
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
