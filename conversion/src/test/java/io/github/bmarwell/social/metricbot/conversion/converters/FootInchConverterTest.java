/*
 * Copyright 2020-2026 The social-metricbot contributors
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class FootInchConverterTest {

    @Test
    public void testBogusInputMatches() {
        // given
        final String tweet = """
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
        final String tweet = """
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

    @Test
    void this_produces_nfe() {
        // given
        final var input = """
            1mi but 1,500 hp.
            12 inches in a foot.
            2 cups water.
            16 oz.
            Based on mpg per passenger seat you used 653.33 gallons that year or 13,785.26 lbs of CO2 that your travels pumped into the atmosphere.
            """;
        final var footInchConverter = new FootInchConverter();

        // when
        final Collection<UnitConversion> matches = footInchConverter.getConvertedUnits(input);

        // then
        Assertions.assertThat(matches).hasSize(3);
    }

    @Test
    void testAfterShouldNotMatchFeet() {
        // given - issue #137
        final var input =
                "I'll take 100 gallons. After last month's power bill, I need to coat my entire house in this!";
        final var footInchConverter = new FootInchConverter();

        // when
        final boolean matches = footInchConverter.matches(input);

        // then
        assertFalse(matches, "The word 'After' should not match as feet/foot");
    }

    @Test
    void testAfterShouldNotConvert() {
        // given - issue #137
        final var input =
                "I'll take 100 gallons. After last month's power bill, I need to coat my entire house in this!";
        final var footInchConverter = new FootInchConverter();

        // when
        final Collection<UnitConversion> conversions = footInchConverter.getConvertedUnits(input);

        // then
        assertTrue(conversions.isEmpty(), "The word 'After' should not produce any conversions");
    }

    @Test
    void testValidFeetFormats() {
        // given - ensure these still work
        final var footInchConverter = new FootInchConverter();

        // when/then
        assertTrue(footInchConverter.matches("10ft"), "10ft should match");
        assertTrue(footInchConverter.matches("10 ft"), "10 ft should match");
        assertTrue(footInchConverter.matches("10 feet"), "10 feet should match");
        assertTrue(footInchConverter.matches("10 foot"), "10 foot should match");
        assertTrue(footInchConverter.matches("5'6\""), "5'6\" should match");
        assertTrue(footInchConverter.matches("a foot"), "a foot should match");
    }

    @Test
    void testUnitWordsAloneShouldNotMatch() {
        // given - these would have matched with the old pattern and hit the feet.isEmpty() check
        final var footInchConverter = new FootInchConverter();

        // when/then - unit words without numbers should not match
        assertFalse(footInchConverter.matches("ft alone"), "bare 'ft' should not match");
        assertFalse(footInchConverter.matches("feet alone"), "bare 'feet' should not match");
        assertFalse(footInchConverter.matches("foot alone"), "bare 'foot' should not match");
        assertFalse(footInchConverter.matches("the ft is"), "bare 'ft' in sentence should not match");
    }

    @Test
    void testUnitWordsAloneShouldNotConvert() {
        // given - ensure no conversions happen for bare unit words
        final var footInchConverter = new FootInchConverter();

        // when/then
        assertTrue(
                footInchConverter.getConvertedUnits("ft alone").isEmpty(), "bare 'ft' should not produce conversions");
        assertTrue(
                footInchConverter.getConvertedUnits("feet alone").isEmpty(),
                "bare 'feet' should not produce conversions");
        assertTrue(
                footInchConverter.getConvertedUnits("foot alone").isEmpty(),
                "bare 'foot' should not produce conversions");
    }
}
