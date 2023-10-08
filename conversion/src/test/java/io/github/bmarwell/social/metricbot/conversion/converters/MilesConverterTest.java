/*
 *  Copyright 2018 The social-metricbot contributors
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

package io.github.bmarwell.social.metricbot.conversion.converters;

import io.github.bmarwell.social.metricbot.conversion.UnitConversion;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class MilesConverterTest {

    private final io.github.bmarwell.social.metricbot.conversion.converters.MilesConverter mc =
            new io.github.bmarwell.social.metricbot.conversion.converters.MilesConverter();

    static Stream<Arguments> tweetsAndUnits() {
        return Stream.of(
                Arguments.of(
                        "2. WTF I got an unsolicited e-mail from a wedding planner 2400 miles away.",
                        "2,400",
                        "3,862.4"),
                Arguments.of("2 distillery's2 projects2 unusual casks10469.70 miles apart", "10,469.7", "16,849.3"),
                Arguments.of("putting down 250 miles in 5 days", "250", "402.3"),
                Arguments.of("It's nearing 12,000 miles", "12,000", "19,312.1"),
                Arguments.of("1-2 mi", "2", "3.2"),
                Arguments.of("-1 to -2 mi", "-2", "-3.2"),
                Arguments.of("1k miles", "1,000", "1,609.3"),
                Arguments.of("What's a mile please", "1", "1.6"),
                Arguments.of("22 mile loop", "22", "35.4"));
    }

    @ParameterizedTest
    @MethodSource("tweetsAndUnits")
    public void testTweet(final String tweet, final String expectedFinding, final String expectedOutput) {
        // given:
        // parameters

        // when:
        final Collection<UnitConversion> convertedUnits = this.mc.getConvertedUnits(tweet);

        // then
        assertThat(convertedUnits)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("inputAmount", expectedFinding)
                .hasFieldOrPropertyWithValue("metricAmount", expectedOutput);
    }

    @Test
    public void specific_tweet_with_2_miles_converions() {
        // given:
        String tweet = "I walk 1mi or 2 miles";

        // when:
        final Collection<UnitConversion> convertedUnits = this.mc.getConvertedUnits(tweet);

        // then:
        assertThat(convertedUnits)
                .hasSize(2)
                .first()
                .hasFieldOrPropertyWithValue("InputAmount", "1")
                .hasFieldOrPropertyWithValue("MetricAmount", "1.6");
    }
}
