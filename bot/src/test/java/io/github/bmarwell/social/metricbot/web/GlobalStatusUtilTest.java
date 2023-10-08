package io.github.bmarwell.social.metricbot.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GlobalStatusUtilTest {

    static Stream<Arguments> tweetsAndExpected() {
        return Stream.of(
                Arguments.of("", "", "", false),
                Arguments.of("Buy at my Boutique\n", "@boutique_seller", "Boutique Seller", true),
                Arguments.of("12 months, 100lbs\n", "100.0", "45.4", false)
                // end
                );
    }

    @ParameterizedTest
    @MethodSource("tweetsAndExpected")
    void contains_blocked_word(
            final String statusText, final String handle, final String displayName, final boolean expected) {
        final var containsBlockedWord = GlobalStatusUtil.containsBlockedWord(statusText, handle, displayName);

        assertThat(containsBlockedWord).isEqualTo(expected);
    }
}
