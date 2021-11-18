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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import io.github.bmhm.twitter.metricbot.conversion.DecimalFormats;
import io.github.bmhm.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class TemperatureConverter implements UsUnitConverter {

  private static final Logger LOG = LoggerFactory.getLogger(TemperatureConverter.class);

  private static final long serialVersionUID = -1586935341494577390L;

  /**
   * Matches &quot;degrees&quot; or just {@code F|°F} or both.
   */
  private static final String DEGREES_F_OR_DEGREES_OR_F = "(?:(?:degree(?:s)?(?:\\s)?(?:F(ahrenheit)?|°F)?)|(?:\\s)?(?:F(ahrenheit)?|°F))";
  /**
   * group '1': matches {@code -2} or {@code 2} etc.
   */
  private static final Pattern degreesFahrenheit =
      Pattern.compile(
          "\\b(([^0-9][-])?(?:[0-9]+\\.)?[0-9]+)(?:\\s)?" + DEGREES_F_OR_DEGREES_OR_F + "\\b",
          Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  private static final String DEGREE_FAHRENHEIT = "°F";
  private static final String DEGREE_CELSIUS = "°C";

  private static final NumberFormat FORMAT_NO_FRAC = DecimalFormats.noFractionDigits();

  public TemperatureConverter() {
    // injection.
  }

  @Override
  public List<String> getSearchTerms() {
    return asList("degrees Fahrenheit", "degree F.");
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

      final UnitConversion unitConversion = ImmutableUnitConversion.builder()
          .inputAmount(FORMAT_NO_FRAC.format(tempFahrenheit))
          .inputUnit(DEGREE_FAHRENHEIT)
          .metricAmount(FORMAT_NO_FRAC.format(tempCelsius))
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
