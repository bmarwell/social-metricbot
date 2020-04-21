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


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.bmhm.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import io.micronaut.context.annotation.Prototype;

@Prototype
public class MilesConverter implements UsUnitConverter {

  private static final Pattern MILES = Pattern.compile(
      "((\\b|[^0-9]-)?([0-9]+,){0,4}([0-9]+\\.)?[0-9]+)( )?(mi(le(s)?)?\\b)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
  private static final String UNIT_MILES = "mi";
  private static final String UNIT_KM = "km";
  private static final double MILES_IN_METERS = 1609.344;

  @Override
  public List<String> getSearchTerms() {
    return asList("miles");
  }

  @Override
  public boolean matches(final String text) {
    return MILES.matcher(text).find();
  }

  @Override
  public Collection<UnitConversion> getConvertedUnits(final String text) {
    final LinkedHashSet<UnitConversion> outputUnits = new LinkedHashSet<>();
    final Matcher matcherMiles = MILES.matcher(text);

    while (matcherMiles.find()) {
      final String group = matcherMiles.group(1).replaceAll(",", "");
      final double miles = Double.parseDouble(group);
      final double meters = miles * MILES_IN_METERS;
      final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);
      df.setMinimumFractionDigits(1);
      df.setMaximumFractionDigits(1);
      df.setRoundingMode(RoundingMode.HALF_UP);
      final String km = df.format(meters / 1000);

      final UnitConversion unitConversion = ImmutableUnitConversion.builder()
          .inputAmount(df.format(miles))
          .inputUnit(UNIT_MILES)
          .metricAmount(km)
          .metricUnit(UNIT_KM)
          .build();

      outputUnits.add(unitConversion);
    }

    return unmodifiableSet(outputUnits);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", MilesConverter.class.getSimpleName() + "[", "]")
        .toString();
  }
}
