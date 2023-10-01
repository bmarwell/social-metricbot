CREATE TABLE TWEET(
                      tweet_id        BIGINT,
                      TWEET_TIME      TIMESTAMP NOT NULL,
                      bot_response_id BIGINT DEFAULT -1,
                      RESPONSE_TIME   TIMESTAMP NOT NULL,
                      PRIMARY KEY (tweet_id)
);
