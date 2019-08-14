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
import io.micronaut.data.annotation.Id;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.time.Instant;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface TweetRepository extends CrudRepository<TweetPdo, Long> {

  void update(@Id Long id, long botResponseId, Instant responseTime);

  List<TweetPdo> findByTweetTimeBefore(Instant createdBefore);

  /**
   * Deletes tweets before given date.
   *
   * <p>Note: int as return cannot be implemented. I wonder why.</p>
   *
   * @param createdBefore
   *     delete tweets before this date.
   */
  void deleteByTweetTimeBefore(Instant createdBefore);
}
