package io.github.bmarwell.twitter.metricbot.web.twitter;

import com.twitter.clientlib.model.Tweet;

import java.io.Serializable;
import java.util.Optional;

public interface TwitterHelper extends Serializable {

    record TwitterUser(String id, long idAsLong, String friendlyName,
                       String screenNameHandle) {
    }


    Tweet createReply(final String replyToId, final String tweetText);

    TwitterUser getTwitterUserById(final String authorId);

    Tweet getTweet(final String tweetId);

    Optional<Tweet> findQuotedTweet(String tweetId);
}
