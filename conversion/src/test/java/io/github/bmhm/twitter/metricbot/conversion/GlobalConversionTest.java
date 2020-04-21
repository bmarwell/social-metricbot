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

import static org.junit.jupiter.api.Assertions.assertTrue;


import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
            List.of("1C=237ml", "2tbsp=29.6mL", "0.25tsp=1.23mL")),
        Arguments.of(
            "180 cal pizza recipe \n"
                + "- 1 mission carb balance tortilla\n"
                + "- 2 tablespoons roa’s homemade pizza sauce (1/4 cup is 4 tablespoons)\n"
                + "- one serving of mozzarella cheese",
            List.of(".25C=59ml", "2tbsp=1ml", "180cal=5J")),
        Arguments.of("5\'9", List.of("5.75ft=175.3cm")),
        Arguments.of("he is 5\'9 or 5\'10 or 6\' tall.", List.of("5.75ft=175.3cm", "5.83ft=177.8cm", "6ft=182.9cm")),
        Arguments.of("he is 5\'9.5\" or 5\'10\" or 6\'.5\" tall.", List.of("5.79ft=176.5cm", "5.83ft=177.8cm", "6.04ft=184.2cm")),
        Arguments.of("he is 6 foot 2 tall.", List.of("6.17ft=188cm")),
        Arguments.of("he is 6 foot 2.5 tall.", List.of("6.21ft=189.2cm")),
        Arguments.of("he is 6 foot 2.5\" tall.", List.of("6.21ft=189.2cm")),
        Arguments.of("he is 6 foot .5\" tall.", List.of("6.04ft=184.2cm")),
        Arguments.of("he is 6 foot 2.5 tall.", List.of("6.21ft=189.2cm")),
        Arguments.of("2 miles", List.of("2.0mi=3.2km")),
        Arguments.of("2 miles or not 2 miles", List.of("2.0mi=3.2km")),
        Arguments.of("4 feet or 16 feet", List.of("4ft=121.9cm", "16ft=4.9m")),
        Arguments.of("12 inches in a foot", List.of("1ft=30.5cm", "12in=304.8mm")),
        Arguments.of("2 cups water\n"
            + "1/4 cup apple cider vinegar\n"
            + "1/2 tsp rock salt", List.of("2C=473ml", ".25C=59ml")),
        Arguments.of("40,000 Feet in the sky", List.of("40,000ft=12.2km"))

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

    Assertions.assertAll(
        () -> expectedOutputs.forEach(
            expectedOutput -> assertTrue(convertedUnits.contains(expectedOutput),
                "expected " + convertedUnits + " to contain " + expectedOutput))
    );
  }

}
