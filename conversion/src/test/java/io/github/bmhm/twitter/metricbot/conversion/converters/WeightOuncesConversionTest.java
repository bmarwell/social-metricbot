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

package io.github.bmhm.twitter.metricbot.conversion.converters;

import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class WeightOuncesConversionTest {

    private final WeightOunceConverter mc = new WeightOunceConverter();

    static Stream<Arguments> tweetsAndUnits() {
        return Stream.of(Arguments.of("then 6-7 ounces per day", "7.0", "198.4"));
    }

    @ParameterizedTest
    @MethodSource("tweetsAndUnits")
    public void testTweet(final String tweet, final String expectedFinding, final String expectedOutput) {
        final Collection<UnitConversion> convertedUnits = this.mc.getConvertedUnits(tweet);

        final Optional<UnitConversion> first = convertedUnits.stream().findFirst();
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, convertedUnits.size()),
                () -> Assertions.assertEquals(
                        expectedFinding,
                        first.map(UnitConversion::getInputAmount).orElse("")),
                () -> Assertions.assertEquals(
                        expectedOutput,
                        first.map(UnitConversion::getMetricAmount).orElse("")));
    }
}
