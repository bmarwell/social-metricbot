/*
 *  Copyright 2018 The twittermetricbot contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.bmhm.twitter.metricbot.db.dao;

import io.github.bmhm.twitter.metricbot.db.pdo.TweetPdo;
import io.github.bmhm.twitter.metricbot.db.pdo.TweetPdo_;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

@Dependent
public class TweetRepository {

    @PersistenceContext(name = "metricbot-persistence-unit")
    private EntityManager entityManager;

    public Optional<TweetPdo> findById(final long tweetId) {
        final TweetPdo tweetPdo = this.entityManager.find(TweetPdo.class, tweetId);

        return Optional.ofNullable(tweetPdo);
    }

    public TweetPdo upsert(final Long tweetId, final long botResponseId, final Instant responseTime) {
        final Optional<TweetPdo> byId = Optional.ofNullable(this.entityManager.find(TweetPdo.class, tweetId));
        if (byId.isPresent()) {
            final TweetPdo foundTweet = byId.orElseThrow();
            foundTweet.setBotResponseId(botResponseId);
            foundTweet.setResponseTime(Instant.now());
            this.entityManager.flush();
            this.entityManager.detach(foundTweet);

            return foundTweet;
        }

        // new
        final TweetPdo tweetPdo = new TweetPdo(tweetId, responseTime, botResponseId);

        return saveNew(tweetPdo);
    }

    private TweetPdo saveNew(final TweetPdo tweetPdo) {
        final EntityManager em = this.entityManager;
        em.persist(tweetPdo);
        em.flush();
        em.detach(tweetPdo);

        return tweetPdo;
    }

    public List<TweetPdo> findByTweetTimeBefore(final Instant createdBefore) {
        // TODO: implement
        throw new UnsupportedOperationException(
                "not yet implemented: [io.github.bmhm.twitter.metricbot.db.dao.TweetRepository::findByTweetTimeBefore].");
    }

    /**
     * Deletes tweets before given date.
     *
     * @param createdBefore delete tweets before this date.
     */
    public int deleteByTweetTimeBefore(final Instant createdBefore) {
        final EntityManager em = this.entityManager;
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaDelete<TweetPdo> criteriaDeleteQuery = cb.createCriteriaDelete(TweetPdo.class);
        final Root<TweetPdo> from = criteriaDeleteQuery.from(TweetPdo.class);
        final ParameterExpression<Instant> beforeParameter = cb.parameter(Instant.class, "createdBefore");
        criteriaDeleteQuery.where(cb.lessThan(from.get(TweetPdo_.tweetTime), beforeParameter));

        final Query deleteQuery = em.createQuery(criteriaDeleteQuery);
        deleteQuery.setParameter(beforeParameter, createdBefore);

        final int executeUpdate = deleteQuery.executeUpdate();
        em.flush();
        return executeUpdate;
    }
}
