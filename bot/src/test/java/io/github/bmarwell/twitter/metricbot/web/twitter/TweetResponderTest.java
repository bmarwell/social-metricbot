package io.github.bmarwell.twitter.metricbot.web.twitter;

import static org.mockito.Mockito.*;

import io.github.bmarwell.twitter.metricbot.conversion.UsConversion;
import io.github.bmarwell.twitter.metricbot.conversion.converters.UsUnitConverter;
import java.util.stream.Stream;
import javax.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import twitter4j.Status;

class TweetResponderTest {

    @Test
    void issue_47_dont_respond_empty() {
        // given:
        var usConversion = new UsConversion();
        Instance<UsUnitConverter> converters = mock(Instance.class);
        usConversion.setConverters(converters);
        // empty converters will end up with an empty response tweet.
        when(converters.stream()).then(args -> Stream.empty());

        var tweetResponder = spy(new TweetResponder());
        tweetResponder.setConverter(usConversion);
        var status = mock(Status.class);
        var statusWithUnits = mock(Status.class);

        // when:
        tweetResponder.doRespond(status, statusWithUnits);

        // then:
        // assume no tweet was send.
        verify(tweetResponder, never()).doRespondToFirst(any(Status.class), any(String.class));
        verify(tweetResponder, never())
                .doRespondTwoPotentallyBoth(any(Status.class), any(Status.class), any(String.class));
    }
}
