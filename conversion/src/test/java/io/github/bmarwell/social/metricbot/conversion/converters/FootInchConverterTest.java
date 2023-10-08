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
import org.assertj.core.api.Assertions;
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

    @Test
    void this_produces_nfe() {
        // given
        final var input =
                """
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
}
