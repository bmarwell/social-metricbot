/*
 * Copyright 2019-2026 The social-metricbot contributors
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.social.metricbot.conversion.UnitConversion;
import java.util.Collection;
import java.util.Iterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemperatureConverterTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void findTemps() {
        final String input = "It is currently Few clouds and 75.1 Degrees Fahrenheit";
        final Collection<UnitConversion> convertedUnits = new TemperatureConverter().getConvertedUnits(input);

        Assertions.assertAll(() -> Assertions.assertEquals(1, convertedUnits.size()));
    }

    @Test
    public void findTemps2() {
        final String input = "It will feel like 108-112°F today";
        final Collection<UnitConversion> convertedUnits = new TemperatureConverter().getConvertedUnits(input);

        final Iterator<UnitConversion> conversionIterator = convertedUnits.iterator();
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, convertedUnits.size()),
                () -> Assertions.assertEquals("112", conversionIterator.next().getInputAmount()));
    }

    @Test
    public void findTemps3() {
        final String input = "scorching 95.0 degrees F. Stay cool";
        final Collection<UnitConversion> convertedUnits = new TemperatureConverter().getConvertedUnits(input);

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, convertedUnits.size()),
                () -> Assertions.assertEquals(
                        "95", convertedUnits.iterator().next().getInputAmount()));
    }

    @Test
    public void findTemps_literal_minus() {
        final String input = "that's minus 40 degrees F. Stay warm.";
        final Collection<UnitConversion> convertedUnits = new TemperatureConverter().getConvertedUnits(input);

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, convertedUnits.size()),
                () -> Assertions.assertEquals(
                        "-40", convertedUnits.iterator().next().getInputAmount()),
                () -> Assertions.assertEquals(
                        "-40", convertedUnits.iterator().next().getMetricAmount()));
    }

    @Test
    public void shouldNotFindTemperatures() {
        // given
        final String input = """
                - 2.25 teaspoons active instant yeast\s
                - 2 large baking potatoes\s
                -3.5 cups of flour\s
                - salt, paprika, sour cream, tons of garlic, & cheese\
                """;

        // when
        final Collection<UnitConversion> convertedUnits = new TemperatureConverter().getConvertedUnits(input);

        // then
        assertTrue(convertedUnits.isEmpty());
    }
}
