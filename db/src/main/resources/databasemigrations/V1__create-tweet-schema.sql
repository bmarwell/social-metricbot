CREATE TABLE TWEET(
    tweet_id                BIGINT,
    TWEET_TIME              TIMESTAMP WITH TIME ZONE  NOT NULL,
    bot_response_id         BIGINT                                DEFAULT -1,
    RESPONSE_TIME           TIMESTAMP WITH TIME ZONE  NULL        DEFAULT NULL,
    PRIMARY KEY (tweet_id)
);
