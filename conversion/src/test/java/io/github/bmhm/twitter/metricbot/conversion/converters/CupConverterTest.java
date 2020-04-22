package io.github.bmhm.twitter.metricbot.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
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
        Arguments.of("\n2/3 cup whole milk")
    );
  }

  @ParameterizedTest
  @MethodSource(value = "shouldMatchArgs")
  void testMatches(final String snippet) {
    // given
    final CupConverter cupConverter = new CupConverter();

    // when
    final boolean matches = cupConverter.matches(snippet);

    // then
    assertTrue(matches);
  }

  @ParameterizedTest
  @MethodSource(value = "shouldMatchArgs")
  void testFind(final String snippet) {
    // given
    final CupConverter cupConverter = new CupConverter();

    // when
    final Collection<UnitConversion> convertedUnits = cupConverter.getConvertedUnits(snippet);

    // then
    assertFalse(convertedUnits.isEmpty());
  }

  @Test
  void patternMatches() {
    // given
    final Pattern fractions = Pattern.compile("[0-9\\u00BC-\\u00BE\\u2150-\\u215E/]+\\s?cup(?:s)?\\b",
        Pattern.CASE_INSENSITIVE);

    // when
    final Matcher matcher = fractions.matcher("¼ CUP (150G)");

    // then
    assertTrue(matcher.find());

  }
}
