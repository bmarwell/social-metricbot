package io.github.bmarwell.twitter.metricbot.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.twitter.metricbot.conversion.UnitConversion;
import java.util.Collection;
import org.junit.jupiter.api.Test;

public class FootInchConverterTest {

    @Test
    public void testBogusInputMatches() {
        // given
        final String tweet =
                """
                Here's the recipe:

                2 cups of Pancake Mix
                1 1/2 cups of Milk
                2 Tsp Vanilla Extract
                1 tsp Cinnamon

                Key lime Icing
                1/2 Cup of confection sugar
                2 tablespoons of sweetened condensed milk
                2 tablespoons of lime Juice

                Garnish\s
                1/2 cup crushed Biscoff cookies\s
                1 lime for lime zest\
                """;
        final FootInchConverter footInchConverter = new FootInchConverter();

        // when
        final boolean matches = footInchConverter.matches(tweet);

        assertFalse(matches);
    }

    @Test
    public void testBogusInputDoesNotConvert() {
        // given
        final String tweet =
                """
                Here's the recipe:

                2 cups of Pancake Mix
                1 1/2 cups of Milk
                2 Tsp Vanilla Extract
                1 tsp Cinnamon

                Key lime Icing
                1/2 Cup of confection sugar
                2 tablespoons of sweetened condensed milk
                2 tablespoons of lime Juice

                Garnish\s
                1/2 cup crushed Biscoff cookies\s
                1 lime for lime zest\
                """;
        final FootInchConverter footInchConverter = new FootInchConverter();

        // when
        final Collection<UnitConversion> matches = footInchConverter.getConvertedUnits(tweet);

        assertTrue(matches.isEmpty());
    }
}
