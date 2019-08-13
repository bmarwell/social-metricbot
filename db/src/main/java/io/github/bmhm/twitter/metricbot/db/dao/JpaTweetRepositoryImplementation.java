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
import io.micronaut.spring.tx.annotation.Transactional;
import java.util.Optional;
import java.util.StringJoiner;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class JpaTweetRepositoryImplementation implements TweetRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  @Transactional(readOnly = true)
  public Optional<TweetPdo> findById(final long tweetId) {
    return Optional.ofNullable(this.entityManager.find(TweetPdo.class, tweetId));
  }

  @Override
  @Transactional
  public TweetPdo save(final TweetPdo toSave) {
    final TweetPdo newTweetPdo = new TweetPdo();
    newTweetPdo.setTweedId(toSave.getTweedId());
    newTweetPdo.setBotResponseId(toSave.getBotResponseId());
    this.entityManager.persist(newTweetPdo);

    return newTweetPdo;
  }

  @Override
  @Transactional
  public TweetPdo save(final long id) {
    final TweetPdo newTweetPdo = new TweetPdo();
    newTweetPdo.setTweedId(id);
    this.entityManager.persist(newTweetPdo);

    return newTweetPdo;
  }

  @Override
  @Transactional
  public TweetPdo update(final int tweetId, final TweetPdo updatedPdo) {
    final TweetPdo newTweetPdo = new TweetPdo();
    newTweetPdo.setTweedId(tweetId);
    newTweetPdo.setBotResponseId(updatedPdo.getBotResponseId());

    this.entityManager.merge(newTweetPdo);

    return newTweetPdo;
  }

  @Override
  @Transactional
  public TweetPdo addReply(final long id, final long replyId) {
    final TweetPdo newTweetPdo = new TweetPdo(id, replyId);

    this.entityManager.merge(newTweetPdo);

    return newTweetPdo;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", JpaTweetRepositoryImplementation.class.getSimpleName() + "[", "]")
        .add("entityManager=" + this.entityManager)
        .toString();
  }
}
