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

import static java.util.Collections.unmodifiableSet;

import io.github.bmhm.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import io.micronaut.context.annotation.Prototype;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class TemperatureConverter implements ImperialUnitConverter {

  private static final Logger LOG = LoggerFactory.getLogger(TemperatureConverter.class);

  /**
   * group '1': matches {@code -2} or {@code 2} etc.
   */
  private static final Pattern degreesFahrenheit = Pattern.compile("((\\b|[^0-9]-)?([0-9]+\\.)?[0-9]+)( )?(degrees F(ahrenheit)?|°F)",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final String DEGREE_FAHRENHEIT = "°F";
  private static final String DEGREE_CELSIUS = "°C";

  public TemperatureConverter() {
    // injection.
  }

  @Override
  public boolean matches(final String text) {
    return degreesFahrenheit.matcher(text).find();
  }

  @Override
  public Collection<UnitConversion> getConvertedUnits(final String text) {
    final Matcher matcher = degreesFahrenheit.matcher(text);
    final LinkedHashSet<UnitConversion> outputUnits = new LinkedHashSet<>();

    while (matcher.find()) {
      final String group = matcher.group(1);
      final double tempFahrenheit;
      if (text.contains("minus " + group)) {
        tempFahrenheit = Double.parseDouble(group) * -1;
      } else {
        tempFahrenheit = Double.parseDouble(group);
      }
      final double tempCelsius = (tempFahrenheit - 32) / (9.0 / 5.0);
      final long tempCelsiusWhole = Math.round(tempCelsius);

      final UnitConversion unitConversion = ImmutableUnitConversion.builder()
          .inputAmount("" + tempFahrenheit)
          .inputUnit(DEGREE_FAHRENHEIT)
          .metricAmount("" + tempCelsiusWhole)
          .metricUnit(DEGREE_CELSIUS)
          .build();
      LOG.debug("Adding: [{}].", unitConversion);
      outputUnits.add(unitConversion);
    }

    return unmodifiableSet(outputUnits);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TemperatureConverter.class.getSimpleName() + "[", "]")
        .toString();
  }
}
