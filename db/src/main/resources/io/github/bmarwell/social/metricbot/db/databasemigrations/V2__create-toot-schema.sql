CREATE TABLE MASTODON_STATUS
(
    STATUS_ID       VARCHAR(64),
    TWEET_TIME      TIMESTAMP   NOT NULL,
    bot_response_id VARCHAR(64) NOT NULL DEFAULT '',
    RESPONSE_TIME   TIMESTAMP   NOT NULL,
    PRIMARY KEY (STATUS_ID)
);
