package io.github.bmarwell.twitter.metricbot.web.twitter;

import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The official twitter client has a very unfriendly API.
 */
@ApplicationScoped
@Default
public class TwitterHelperImpl implements TwitterHelper, Serializable {

    @Serial
    private static final long serialVersionUID = -1565875663182474816L;

    private static final Logger LOG = LoggerFactory.getLogger(TwitterHelperImpl.class);

    public record WrappedTweet(Instant savedAt, Tweet tweet) {
    }

    private final Map<String, TwitterUser> cachedUsers = new ConcurrentHashMap<>();

    private final Map<String, WrappedTweet> cachedTweets = new ConcurrentHashMap<>();

    @Inject
    private TwitterApi twitter;

    @Override
    public Tweet createReply(final String replyToId, final String tweetText) {
        try {

            final CreateTweetRequestReply replyRequest = new CreateTweetRequestReply()
                .inReplyToTweetId(replyToId);
            final CreateTweetRequest createTweetRequest = new CreateTweetRequest()
                .text(tweetText)
                .reply(replyRequest);

            final TweetCreateResponse createResponse = twitter.tweets().createTweet(createTweetRequest);

            final List<Problem> errors = createResponse.getErrors();
            if (errors != null && !errors.isEmpty()) {
                LOG.error("Problem sending tweet.");
                for (final Problem error : errors) {
                    LOG.error("error: [{}].", error);
                }

                throw new IllegalStateException("Errors");
            }

            final var response = createResponse.getData();

            return this.getTweet(response.getId());
        } catch (final ApiException apiException) {
            throw new IllegalStateException("Unable to create tweet  in reply to " + replyToId + ", because: " + apiException.getMessage(), apiException);
        }
    }

    @Override
    public TwitterUser getTwitterUserById(final String authorId) {
        final TwitterUser twitterUser = cachedUsers.get(authorId);
        if (twitterUser != null) {
            shrinkCaches();
            return twitterUser;
        }

        // user not locally cached
        final var retrievedUser = doGetTwitterUser(authorId);
        this.cachedUsers.put(authorId, retrievedUser);

        shrinkCaches();

        return retrievedUser;
    }

    @Override
    public Tweet getTweet(final String tweetId) {
        final WrappedTweet cachedTweet = this.cachedTweets.get(tweetId);
        if (cachedTweet != null) {
            final Tweet foundTweet = cachedTweet.tweet();
            shrinkCaches();
            return foundTweet;
        }

        // not locally cached
        final var tweet = doGetTweet(tweetId);
        this.cachedTweets.put(tweetId, new WrappedTweet(Instant.now(), tweet));

        shrinkCaches();

        return tweet;
    }

    @Override
    public Optional<Tweet> findQuotedTweet(final String tweetId) {
        final Tweet sourceTweet = getTweet(tweetId);
        final List<TweetReferencedTweets> referencedTweets = sourceTweet.getReferencedTweets();
        final Optional<TweetReferencedTweets> quotedTweetRef = referencedTweets.stream()
            .filter(rt -> rt.getType() == TweetReferencedTweets.TypeEnum.QUOTED)
            .findFirst();

        if (quotedTweetRef.isEmpty()) {
            return Optional.empty();
        }

        final TweetReferencedTweets quotedTweet = quotedTweetRef.orElseThrow();
        final String quotedTweetId = quotedTweet.getId();

        return Optional.ofNullable(getTweet(quotedTweetId));
    }

    private void shrinkCaches() {
        final var oldBefore = Instant.now().minusSeconds(3600);

        final var oldEntryIds = cachedTweets.entrySet().stream()
            .filter(e -> e.getValue().savedAt().isBefore(oldBefore))
            .map(Entry::getKey)
            .toList();

        oldEntryIds.forEach(this.cachedTweets::remove);
    }

    private Tweet doGetTweet(final String tweetId) {
        try {
            final var tweetResponse = this.twitter.tweets()
                .findTweetById(tweetId, null, null, null, null, null, null);
            final List<Problem> errors = tweetResponse.getErrors();
            if (errors != null && !errors.isEmpty()) {
                throw new IllegalStateException(
                    "Unable to retrieve tweetid = " + tweetId + ", because: " + errors);
            }

            return tweetResponse.getData();
        } catch (final ApiException apiException) {
            throw new IllegalStateException(
                "Unable to retrieve tweet id = " + tweetId + ", because: "
                    + apiException.getMessage(),
                apiException);
        }
    }

    private TwitterUser doGetTwitterUser(final String userId) {
        try {
            final SingleUserLookupResponse userById = this.twitter.users()
                .findUserById(userId, null, null, null);
            final List<Problem> errors = userById.getErrors();

            if (errors != null && !errors.isEmpty()) {
                throw new IllegalStateException(
                    "Unable to retrieve userid = " + userId + ", because: " + errors);
            }
            final var user = userById.getData();

            return new TwitterUser(userId, Long.parseLong(userId, 10), user.getName(),
                user.getUsername());
        } catch (final ApiException apiException) {
            throw new IllegalStateException(
                "Unable to retrieve user id = " + userId + ", because: "
                    + apiException.getMessage(), apiException);
        }
    }
}
