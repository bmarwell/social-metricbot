package io.github.bmarwell.social.metricbot.conversion.converters;

import io.github.bmarwell.social.metricbot.conversion.UnitConversion;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PoundConverterTest {

    private final PoundConverter pc = new PoundConverter();

    static Stream<Arguments> tweetsAndUnits() {
        return Stream.of(
                Arguments.of("2 months, 18lbs\n", "18.0", "8.2"), Arguments.of("12 months, 100lbs\n", "100.0", "45.4"));
    }

    @ParameterizedTest
    @MethodSource("tweetsAndUnits")
    public void testTweet(final String tweet, final String expectedFinding, final String expectedOutput) {
        final Collection<UnitConversion> convertedUnits = this.pc.getConvertedUnits(tweet);

        final Optional<UnitConversion> first = convertedUnits.stream().findFirst();
        String sourceUnit = first.map(UnitConversion::getInputAmount).orElse("");
        String targetUnit = first.map(UnitConversion::getMetricAmount).orElse("");

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(convertedUnits).as("number of conversions").hasSize(1);
        softly.assertThat(sourceUnit).as("Input amount recognized").isEqualTo(expectedFinding);
        softly.assertThat(targetUnit).as("Output amount correct").isEqualTo(expectedOutput);

        softly.assertAll();
    }
}
