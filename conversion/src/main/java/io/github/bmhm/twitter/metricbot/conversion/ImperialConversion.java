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

package io.github.bmhm.twitter.metricbot.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ImperialConversion {

  /**
   * first group: matches &qout;-2&qout; or &qout;2&qout; etc.
   * 2nd group: matches &qout;-&qout; or &qout;word beginning&qout;.
   */
  public static final Pattern degreesFahrenheit = Pattern.compile("((\\b|-)?[0-9]+) degrees Fahrenheit",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE );

  public ImperialConversion() {
    //
  }

  public String returnConverted(String input) {
    List<String> outputUnits = new ArrayList<>();
    final Matcher degreesFMatcher = degreesFahrenheit.matcher(input);
    if (degreesFMatcher.find()) {
      final long tempFahrenheit = Long.parseLong(degreesFMatcher.group(1));
      double tempCelsius = (tempFahrenheit - 32) / (9.0 / 5.0);
      long tempCelsiusWhole = Math.round(tempCelsius);
      outputUnits.add(String.format("%d°F = %d°C", tempFahrenheit, tempCelsiusWhole));
    }

    return String.join(", ", outputUnits);
  }
}
