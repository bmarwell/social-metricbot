package io.github.bmhm.twitter.metricbot.web.twitter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.bmhm.twitter.metricbot.common.TwitterConfig;
import java.sql.Date;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import twitter4j.Status;
import twitter4j.User;

class MentionEventHandlerTest {

    private TwitterConfig tc;
    private MentionEventHandler mev;

    @BeforeEach
    void setup() {
        this.tc = createTwitterConfig();
        this.mev = new MentionEventHandler();
        this.mev.setTwitterConfig(this.tc);
    }

    @ParameterizedTest
    @CsvSource(value = {"@metricbot1 please,true", "@metricbot1,false"})
    void issue_48_expects_please(String tweetText, boolean shouldReplyTo) {
        // given:
        var status = createMockStatus(tweetText);

        // when:
        boolean shouldRespondTo = this.mev.shouldRespondTo(status);

        // then:
        Assertions.assertThat(shouldRespondTo).isEqualTo(shouldReplyTo);
    }

    private Status createMockStatus(String statusText) {
        var user = mock(User.class);
        when(user.getName()).then(args -> "someRandomDude");

        var status = mock(Status.class);
        when(status.getText()).then(args -> statusText);
        when(status.getCreatedAt()).then(args -> Date.from(Instant.now()));
        when(status.getUser()).then(args -> user);

        return status;
    }

    private TwitterConfig createTwitterConfig() {
        TwitterConfig twitterConfig = new TwitterConfig();
        twitterConfig.setAccountName("metricbot1");

        return twitterConfig;
    }
}
