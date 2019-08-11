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

package io.github.bmhm.twitter.metricbot.twitter;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import java.util.StringJoiner;
import javax.inject.Singleton;

@Singleton
public class TwitterConfig {

  @Property(name = "io.github.bmhm.twitter.metricbot.debug")
  private boolean debug;

  @Property(name = "io.github.bmhm.twitter.metricbot.consumerkey")
  private String consumerKey;

  @Property(name = "io.github.bmhm.twitter.metricbot.consumersecret")
  private String consumerSecret;

  @Property(name = "io.github.bmhm.twitter.metricbot.accesstoken")
  private String accessToken;

  @Property(name = "io.github.bmhm.twitter.metricbot.accesstokensecret")
  private String accessTokenSecret;

  public TwitterConfig() {
    // injection constructor.
  }

  public boolean isDebug() {
    return this.debug;
  }

  public String getConsumerKey() {
    return this.consumerKey;
  }

  public String getConsumerSecret() {
    return this.consumerSecret;
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public String getAccessTokenSecret() {
    return this.accessTokenSecret;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TwitterConfig.class.getSimpleName() + "[", "]")
        .add("debug=" + debug)
        .add("consumerKey='" + consumerKey + "'")
        .add("consumerSecret='" + consumerSecret + "'")
        .add("accessToken='" + accessToken + "'")
        .add("accessTokenSecret='" + accessTokenSecret + "'")
        .toString();
  }
}
