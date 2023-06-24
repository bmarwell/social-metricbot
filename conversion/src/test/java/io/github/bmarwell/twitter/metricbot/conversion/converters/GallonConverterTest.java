package io.github.bmarwell.twitter.metricbot.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.twitter.metricbot.conversion.UnitConversion;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GallonConverterTest {

    static Stream<Arguments> shouldMatchArgs() {
        return Stream.of(
                Arguments.of("A Boeing 747 airplane burns ~1 gallon of fuel every second.\n"),
                Arguments.of("It will burn 18,000 gallons of fuel during a 5-hr flight.\n"),
                Arguments.of("I also got a gallon pitcher so I can make a bunch of Gatorade.\n"),
                Arguments.of("A single beaver pond holds an estimated 1.1 million gallons of water."),
                Arguments.of("1 gallon cherries and another 2 gallons of strawberries picked today."
                        + "2 gallons of strawberries picked yesterday. Strawberries going in the freezer.\n"),
                Arguments.of("It pumps 1500 gallons a minute from 240 ft deep"));
    }

    @ParameterizedTest
    @MethodSource(value = "shouldMatchArgs")
    void testMatches(final String snippet) {
        // given
        final GallonConverter usConverter = new GallonConverter();

        // when
        final boolean matches = usConverter.matches(snippet);

        // then
        assertTrue(matches);
    }

    @ParameterizedTest
    @MethodSource(value = "shouldMatchArgs")
    void testFind(final String snippet) {
        // given
        final GallonConverter usConverter = new GallonConverter();

        // when
        final Collection<UnitConversion> convertedUnits = usConverter.getConvertedUnits(snippet);

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
