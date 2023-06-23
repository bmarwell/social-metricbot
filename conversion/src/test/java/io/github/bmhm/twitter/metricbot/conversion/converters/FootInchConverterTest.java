package io.github.bmhm.twitter.metricbot.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import java.util.Collection;
import org.junit.jupiter.api.Test;

public class FootInchConverterTest {

    @Test
    public void testBogusInputMatches() {
        // given
        final String tweet = "Here's the recipe:\n"
                + "\n"
                + "2 cups of Pancake Mix\n"
                + "1 1/2 cups of Milk\n"
                + "2 Tsp Vanilla Extract\n"
                + "1 tsp Cinnamon\n"
                + "\n"
                + "Key lime Icing\n"
                + "1/2 Cup of confection sugar\n"
                + "2 tablespoons of sweetened condensed milk\n"
                + "2 tablespoons of lime Juice\n"
                + "\n"
                + "Garnish \n"
                + "1/2 cup crushed Biscoff cookies \n"
                + "1 lime for lime zest";
        final FootInchConverter footInchConverter = new FootInchConverter();

        // when
        final boolean matches = footInchConverter.matches(tweet);

        assertFalse(matches);
    }

    @Test
    public void testBogusInputDoesNotConvert() {
        // given
        final String tweet = "Here's the recipe:\n"
                + "\n"
                + "2 cups of Pancake Mix\n"
                + "1 1/2 cups of Milk\n"
                + "2 Tsp Vanilla Extract\n"
                + "1 tsp Cinnamon\n"
                + "\n"
                + "Key lime Icing\n"
                + "1/2 Cup of confection sugar\n"
                + "2 tablespoons of sweetened condensed milk\n"
                + "2 tablespoons of lime Juice\n"
                + "\n"
                + "Garnish \n"
                + "1/2 cup crushed Biscoff cookies \n"
                + "1 lime for lime zest";
        final FootInchConverter footInchConverter = new FootInchConverter();

        // when
        final Collection<UnitConversion> matches = footInchConverter.getConvertedUnits(tweet);

        assertTrue(matches.isEmpty());
    }
}
