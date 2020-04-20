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

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

import io.github.bmhm.twitter.metricbot.db.pdo.TweetPdo;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.H2)
public abstract class TweetRepository implements CrudRepository<TweetPdo, Long> {

  @Transactional
  public abstract void update(@Id Long id, long botResponseId, Instant responseTime);

  @Transactional
  public TweetPdo save(@Id final Long id, final long botResponseId, final Instant responseTime) {
    final TweetPdo tweetPdo = new TweetPdo(id, responseTime, botResponseId);

    return save(tweetPdo);
  }

  public abstract List<TweetPdo> findByTweetTimeBefore(Instant createdBefore);

  /**
   * Deletes tweets before given date.
   *
   * <p>Note: int as return cannot be implemented. I wonder why.</p>
   *
   * @param createdBefore
   *     delete tweets before this date.
   */
  public abstract void deleteByTweetTimeBefore(Instant createdBefore);
}
