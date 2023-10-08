/*
 * Copyright 2020-2023 The social-metricbot contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bmarwell.social.metricbot.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.social.metricbot.conversion.UnitConversion;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CupConverterTest {

    static Stream<Arguments> shouldMatchArgs() {
        return Stream.of(
                Arguments.of("2 cups self-rising flour"),
                Arguments.of("1/3 cup vegetable oil\n"),
                Arguments.of("⅔ CUP (150G) vegetable oil\n"),
                Arguments.of("¾ CUP (60G) vegetable oil\n"),
                Arguments.of("⅓ CUP (115G) vegetable oil\n"),
                Arguments.of("\n2/3 cup whole milk"));
    }

    @ParameterizedTest
    @MethodSource(value = "shouldMatchArgs")
    void testMatches(final String snippet) {
        // given
        final io.github.bmarwell.social.metricbot.conversion.converters.CupConverter cupConverter =
                new io.github.bmarwell.social.metricbot.conversion.converters.CupConverter();

        // when
        final boolean matches = cupConverter.matches(snippet);

        // then
        assertTrue(matches);
    }

    @ParameterizedTest
    @MethodSource(value = "shouldMatchArgs")
    void testFind(final String snippet) {
        // given
        final io.github.bmarwell.social.metricbot.conversion.converters.CupConverter cupConverter =
                new io.github.bmarwell.social.metricbot.conversion.converters.CupConverter();

        // when
        final Collection<UnitConversion> convertedUnits = cupConverter.getConvertedUnits(snippet);

        // then
        assertFalse(convertedUnits.isEmpty());
    }

    @Test
    void patternMatches() {
        // given
        final Pattern fractions =
                Pattern.compile("[0-9\\u00BC-\\u00BE\\u2150-\\u215E/]+\\s?cup(?:s)?\\b", Pattern.CASE_INSENSITIVE);

        // when
        final Matcher matcher = fractions.matcher("¼ CUP (150G)");

        // then
        assertTrue(matcher.find());
    }
}
