package io.github.bmarwell.twitter.metricbot.conversion.converters;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.twitter.metricbot.conversion.UnitConversion;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GallonConverterTest {

    static Stream<Arguments> shouldMatchArgs() {
        return Stream.of(
                Arguments.of("A Boeing 747 airplane burns ~1 gallon of fuel every second.\n", List.of("3.79L")),
                Arguments.of("It will burn 18,000 gallons of fuel during a 5-hr flight.\n", List.of("68.14m³")),
                Arguments.of("I also got a gallon pitcher so I can make a bunch of Gatorade.\n", List.of("3.79L")),
                Arguments.of(
                        "A single beaver pond holds an estimated 1.1 million gallons of water.", List.of("4,163.95m³")),
                Arguments.of(
                        "1 gallon cherries and another 2 gallons of strawberries picked today."
                                + "2 gallons of strawberries picked yesterday. Strawberries going in the freezer.\n",
                        List.of("3.79L", "7.57L")),
                Arguments.of("It pumps 1500 gallons a minute from 240 ft deep", List.of("5.68m³")),
                Arguments.of(
                        "Aaahhh - amazing! 2.4 billion gallons have erupted into Kilauea in the last week ish!\n",
                        List.of("9,084,988.31m³")),
                Arguments.of("They kicked em out and flooded it under 1.5 billion gallons", List.of("5,678,117.69m³")),
                Arguments.of("20,000 gallons of fuel seems like a lot.\n", List.of("75.71m³")));
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
    void testFind(final String snippet, List<String> expected) {
        // given
        final GallonConverter usConverter = new GallonConverter();

        // when
        final Collection<UnitConversion> convertedUnits = usConverter.getConvertedUnits(snippet);

        // then
        assertFalse(convertedUnits.isEmpty());
        final String outputString = convertedUnits.stream()
                .map(cu -> cu.getMetricAmount() + cu.getMetricUnit())
                .collect(Collectors.joining(", "));
        assertThat(outputString).contains(expected);
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
