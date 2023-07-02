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

package io.github.bmarwell.twitter.metricbot.web.factory;

import io.github.bmarwell.twitter.metricbot.common.TwitterConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Produces instances of Twitter clients.
 *
 * <p>Called this factory 'producer', to avoid name clash with {@link twitter4j.TwitterFactory},
 * and because in CDI you'd call this a producer method anyway.</p>
 */
@ApplicationScoped
public class TwitterProducer {

    @Inject
    private TwitterConfig twitterAttributes;

    @Produces
    @ApplicationScoped
    public Twitter getTwitter() {
        if (this.twitterAttributes == null
                || this.twitterAttributes.getConsumerKey() == null
                || this.twitterAttributes.getAccessToken() == null) {
            throw new IllegalStateException("not configured");
        }

        final ConfigurationBuilder cb = new ConfigurationBuilder()
                .setDebugEnabled(this.twitterAttributes.isDebug())
                .setOAuthConsumerKey(this.twitterAttributes.getConsumerKey())
                .setOAuthConsumerSecret(this.twitterAttributes.getConsumerSecret())
                .setOAuthAccessToken(this.twitterAttributes.getAccessToken())
                .setOAuthAccessTokenSecret(this.twitterAttributes.getAccessTokenSecret());
        final twitter4j.TwitterFactory tf = new twitter4j.TwitterFactory(cb.build());

        return tf.getInstance();
    }

    @Produces
    @ApplicationScoped
    public AsyncTwitterFactory getAsyncTwitter() {
        if (this.twitterAttributes == null
                || this.twitterAttributes.getConsumerKey() == null
                || this.twitterAttributes.getAccessToken() == null) {
            throw new IllegalStateException("not configured");
        }

        final ConfigurationBuilder cb = new ConfigurationBuilder()
                .setDebugEnabled(this.twitterAttributes.isDebug())
                .setOAuthConsumerKey(this.twitterAttributes.getConsumerKey())
                .setOAuthConsumerSecret(this.twitterAttributes.getConsumerSecret())
                .setOAuthAccessToken(this.twitterAttributes.getAccessToken())
                .setOAuthAccessTokenSecret(this.twitterAttributes.getAccessTokenSecret());

        return new AsyncTwitterFactory(cb.build());
    }

    @ApplicationScoped
    @Named("recentMatches")
    public Map<Long, Instant> recentMatches() {
        return new ConcurrentHashMap<>();
    }
}
