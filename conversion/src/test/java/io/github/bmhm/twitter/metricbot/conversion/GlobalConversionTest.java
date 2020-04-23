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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;


import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalConversionTest {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalConversionTest.class);

  static Stream<Arguments> tweetsAndUnits() {
    return Stream.of(
        Arguments.of(
            "1 cup oats\n2 bananas mashed\n2 tbsp peanut butter\n1/4 tsp vanilla extract",
            List.of("1C=237ml", "2tbsp=30g", "0.25tsp=1g")),
        Arguments.of(
            "180 cal pizza recipe \n"
                + "- 1 mission carb balance tortilla\n"
                + "- 2 tablespoons roa’s homemade pizza sauce (1/4 cup is 4 tablespoons)\n"
                + "- one serving of mozzarella cheese",
            List.of(".25C=59ml", "2tbsp=30g", "180cal=753kJ")),
        Arguments.of("5\'9", List.of("5.75'=175.3cm")),
        Arguments.of("he is 5\'9 or 5\'10 or 6\' tall.", List.of("5.75'=175.3cm", "5.83'=177.8cm", "6'=182.9cm")),
        Arguments.of("he is 5\'9.5\" or 5\'10\" or 6\'.5\" tall.", List.of("5.79'=176.5cm", "5.83'=177.8cm", "6.04'=184.2cm")),
        Arguments.of("he is 6 foot 2 tall.", List.of("6.17'=188cm")),
        Arguments.of("he is 6 foot 2.5 tall.", List.of("6.21'=189.2cm")),
        Arguments.of("he is 6 foot 2.5\" tall.", List.of("6.21'=189.2cm")),
        Arguments.of("he is 6 foot .5\" tall.", List.of("6.04'=184.2cm")),
        Arguments.of("he is 6 foot 2.5 tall.", List.of("6.21'=189.2cm")),
        Arguments.of("2 miles", List.of("2mi=3.2km")),
        Arguments.of("2 miles or not 2 miles", List.of("2mi=3.2km")),
        Arguments.of("4 feet or 16 feet", List.of("4'=121.9cm", "16'=4.9m")),
        Arguments.of("12 inches in a foot", List.of("1'=30.5cm", "12\"=30.5cm")),
        Arguments.of("2 cups water\n"
            + "1/4 cup apple cider vinegar\n"
            + "1/2 tsp rock salt", List.of("2C=473ml", ".25C=59ml")),
        Arguments.of("40,000 Feet in the sky", List.of("40,000'=12.2km")),
        Arguments.of("a 21 inch waist and actual abs  (currently 23 inches)", List.of("21\"=53.3cm", "23\"=58.4")),
        Arguments.of("1 cup of self rising flour\n"
            + "1 tsp of vanilla extract\n"
            + "1/4 cup sugar\n"
            + "*3/4 cup powdered sugar\n"
            + "2 TBSP  butter\n"
            + "* 4 TBSP coconut oil\n"
            + "Pineapple 16 oz", List.of("1tsp=4g", "0.25C=59ml", "0.75C=177ml", "2tbsp=30g", "4tbsp=60g", "16.0oz=453.6g")),
        Arguments.of("▪️4 cups cold water\n"
                + "▪️1/4 cup salt\n"
                + "▪️1/3 cup maple syrup\n"
                + "▪️3 cloves garlic, crushed\n"
                + "▪️3 tablespoons chopped fresh ginger\n"
                + "▪️2 teaspoons dried rosemary\n"
                + "▪️1 tablespoon cracked black pepper\n"
                + "▪️1/2 teaspoon red pepper flake",
            List.of("4C=946ml", "0.25C=59ml", "0.33C=78ml", "2tsp=8g", "0.5tsp=2g", "3tbsp=45g", "1tbsp=15g")),
        Arguments.of("2 CUPS (180G) ROLLED OATS\n"
                + "\n"
                + "1 CUP (150G) PLAIN (ALL-PURPOSE) FLOUR \n"
                + "⅔ CUP (150G) CASTER (SUPERFINE) SUGAR \n"
                + "¾ CUP (60G) DESICCATED COCONUT \n"
                + "⅓ CUP (115G) GOLDEN SYRUP \n"
                + "125G UNSALTED BUTTER ",
            List.of("2C=473ml", "1C=237ml", "0.67C=158ml", "0.75C=177ml", "0.33C=79ml")),
        // horse powers
        Arguments.of(
            "This treadmill features a powerful 4.0 HP motor",
            List.of("4hp=3kW")),
        Arguments.of(
            "and up to 1,500 hp",
            List.of("1,500hp=1,118.5kW")),
        Arguments.of(
            "with a view counter in 2020\uD83D\uDE02700 miles, 1,500 hp, 0-60 2.9 seconds. ",
            List.of("1,500hp=1,118.5kW", "700mi=1,126.5km")),
        Arguments.of(
            "my two mile time. ",
            List.of("2mi=3.2km"))

    );
  }

  @Inject UsConversion conversion;


  @BeforeEach
  public void setUp() {
    ApplicationContext.build().start().inject(this);
  }


  @Test
  public void setUpTestTest() {
    Assertions.assertFalse(this.conversion.getConverters().isEmpty());
  }

  @ParameterizedTest
  @MethodSource("tweetsAndUnits")
  public void testTweet(final String tweet, final List<String> expectedOutputs) {
    final String convertedUnits = this.conversion.returnConverted(tweet);

    final Stream<Executable> assertions = expectedOutputs.stream()
        .map(expectedConversion -> makeAssertion(convertedUnits, expectedConversion));

    LOG.debug("Checking tweet [{}].", tweet);

    assertAll(
        "Checking that list "
            + convertedUnits + " contains each of these: " + expectedOutputs + ".\n"
            + "Original Tweet: [" + tweet.replaceAll("\n", "\\\\n") + "].\n",
        assertions);
  }

  private org.junit.jupiter.api.function.Executable makeAssertion(final String convertedUnits, final String expectedConversion) {
    return () -> assertTrue(convertedUnits.contains(expectedConversion),
        "expected [" + convertedUnits + "] to contain **[" + expectedConversion + "]**.");
  }

}
