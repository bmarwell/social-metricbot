/*
 *  Copyright 2018 The twittermetricbot contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.bmhm.twitter.metricbot.conversion.converters;

import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemperatureConverterTest {

  private static final Logger LOG = LoggerFactory.getLogger(TemperatureConverterTest.class);

  @Test
  public void findTemps() {
    final String input = "It is currently Few clouds and 75.1 Degrees Fahrenheit";
    final Collection<UnitConversion> convertedUnits = new TemperatureConverter().getConvertedUnits(input);

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, convertedUnits.size())
    );
  }

  @Test
  public void findTemps2() {
    final String input = "It will feel like 108-112°F today";
    final Collection<UnitConversion> convertedUnits = new TemperatureConverter().getConvertedUnits(input);

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, convertedUnits.size()),
        () -> Assertions.assertEquals("112.0", convertedUnits.iterator().next().getInputAmount())
    );
  }

  @Test
  public void findTemps3() {
    final String input = "scorching 95.0 degrees F. Stay cool";
    final Collection<UnitConversion> convertedUnits = new TemperatureConverter().getConvertedUnits(input);

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, convertedUnits.size()),
        () -> Assertions.assertEquals("95.0", convertedUnits.iterator().next().getInputAmount())
    );
  }


  @Test
  public void findTemps_literal_minus() {
    final String input = "that's minus 40 degrees F. Stay warm.";
    final Collection<UnitConversion> convertedUnits = new TemperatureConverter().getConvertedUnits(input);

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, convertedUnits.size()),
        () -> Assertions.assertEquals("-40.0", convertedUnits.iterator().next().getInputAmount()),
        () -> Assertions.assertEquals("-40", convertedUnits.iterator().next().getMetricAmount())
    );
  }
}
