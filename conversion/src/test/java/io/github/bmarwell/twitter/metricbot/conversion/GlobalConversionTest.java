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

package io.github.bmarwell.twitter.metricbot.conversion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.bmarwell.twitter.metricbot.conversion.converters.*;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
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
                        List.of("1C=237ml", "2tbsp=30g", "0.25tsp=1g")),
                Arguments.of(
                        """
                        180 cal pizza recipe\s
                        - 1 mission carb balance tortilla
                        - 2 tablespoons roa’s homemade pizza sauce (1/4 cup is 4 tablespoons)
                        - one serving of mozzarella cheese\
                        """,
                        List.of(".25C=59ml", "2tbsp=30g", "180cal=753kJ")),
                Arguments.of("5\'9", List.of("5.75'=175.3cm")),
                Arguments.of(
                        "he is 5\'9 or 5\'10 or 6\' tall.", List.of("5.75'=175.3cm", "5.83'=177.8cm", "6'=182.9cm")),
                Arguments.of(
                        "he is 5\'9.5\" or 5\'10\" or 6\'.5\" tall.",
                        List.of("5.79'=176.5cm", "5.83'=177.8cm", "6.04'=184.2cm")),
                Arguments.of("he is 6 foot 2 tall.", List.of("6.17'=188cm")),
                Arguments.of("he is 6 foot 2.5 tall.", List.of("6.21'=189.2cm")),
                Arguments.of("he is 6 foot 2.5\" tall.", List.of("6.21'=189.2cm")),
                Arguments.of("he is 6 foot .5\" tall.", List.of("6.04'=184.2cm")),
                Arguments.of("he is 6 foot 2.5 tall.", List.of("6.21'=189.2cm")),
                Arguments.of("2 miles", List.of("2mi=3.2km")),
                Arguments.of("2 miles or not 2 miles", List.of("2mi=3.2km")),
                Arguments.of("4 feet or 16 feet", List.of("4'=121.9cm", "16'=4.9m")),
                Arguments.of("12 inches in a foot", List.of("1'=30.5cm", "12\"=30.5cm")),
                Arguments.of(
                        """
                        ... done!
                        https://github.com/bmarwell/twitter-metricbot/pull/114

                        Have fun,\s
                        @gmzabos
                        !

                        @metricbot1
                         please convert 2pt and 4 pt.
                        """,
                        List.of("2pt=946.35ml", "4pt=1.89L")),
                Arguments.of(
                        """
                        2 cups water
                        1/4 cup apple cider vinegar
                        1/2 tsp rock salt\
                        """,
                        List.of("2C=473ml", ".25C=59ml")),
                Arguments.of("40,000 Feet in the sky", List.of("40,000'=12.2km")),
                Arguments.of("2pt", List.of("2pt=946.35ml")),
                Arguments.of("3pt", List.of("3pt=1.42L")),
                Arguments.of("35 pt", List.of("35pt=16.56L")),
                Arguments.of(
                        "a 21 inch waist and actual abs  (currently 23 inches)", List.of("21\"=53.3cm", "23\"=58.4")),
                Arguments.of(
                        """
                        1 cup of self rising flour
                        1 tsp of vanilla extract
                        1/4 cup sugar
                        *3/4 cup powdered sugar
                        2 TBSP  butter
                        * 4 TBSP coconut oil
                        Pineapple 16 oz\
                        """,
                        List.of("1tsp=4g", "0.25C=59ml", "0.75C=177ml", "2tbsp=30g", "4tbsp=60g", "16.0oz=453.6g")),
                Arguments.of(
                        """
                        ▪️4 cups cold water
                        ▪️1/4 cup salt
                        ▪️1/3 cup maple syrup
                        ▪️3 cloves garlic, crushed
                        ▪️3 tablespoons chopped fresh ginger
                        ▪️2 teaspoons dried rosemary
                        ▪️1 tablespoon cracked black pepper
                        ▪️1/2 teaspoon red pepper flake\
                        """,
                        List.of(
                                "4C=946ml",
                                "0.25C=59ml",
                                "0.33C=78ml",
                                "2tsp=8g",
                                "0.5tsp=2g",
                                "3tbsp=45g",
                                "1tbsp=15g")),
                Arguments.of(
                        """
                        2 CUPS (180G) ROLLED OATS

                        1 CUP (150G) PLAIN (ALL-PURPOSE) FLOUR\s
                        ⅔ CUP (150G) CASTER (SUPERFINE) SUGAR\s
                        ¾ CUP (60G) DESICCATED COCONUT\s
                        ⅓ CUP (115G) GOLDEN SYRUP\s
                        125G UNSALTED BUTTER \
                        """,
                        List.of("2C=473ml", "1C=237ml", "0.67C=158ml", "0.75C=177ml", "0.33C=79ml")),
                // horse powers
                Arguments.of("This treadmill features a powerful 4.0 HP motor", List.of("4hp=3kW")),
                Arguments.of("and up to 1,500 hp", List.of("1,500hp=1,118.5kW")),
                Arguments.of(
                        "with a view counter in 2020\uD83D\uDE02700 miles, 1,500 hp, 0-60 2.9 seconds. ",
                        List.of("1,500hp=1,118.5kW", "700mi=1,126.5km")),
                Arguments.of("my two mile time. ", List.of("2mi=3.2km")),
                Arguments.of(
                        """
                        - five mile walk
                        - With no food in body
                        - In 84 degree weather\
                        """,
                        List.of("5mi=8km", "84°F=29°C")),
                Arguments.of(
                        """
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
                        """,
                        List.of("2C=473ml", "0.5C=118ml", "2tsp=8g", "1tsp=4g", "2tbsp=30g")),
                Arguments.of(
                        """
                        - 2.25 teaspoons active instant yeast\s
                        - 2 large baking potatoes\s
                        -3.5 cups of flour\s
                        - salt, paprika, sour cream, tons of garlic, & cheese\
                        """,
                        List.of("2.25tsp=9g", "3.5C=828ml")),
                Arguments.of(
                        "It pumps 1500 gallons a minute from 240 ft deep", List.of("1,500gal=5.68m³", "240ft=73.2m")),
                Arguments.of(
                        """
                        1 gallon cherries and another 2 gallons of strawberries picked today.\
                        2 gallons of strawberries picked yesterday.\
                        """,
                        List.of("1gal=3.79L", "2gal=7.57L")),
                Arguments.of(
                        """
                        Based on mpg per passenger seat you used 653.33 gallons that year\
                         or 13,785.26 lbs of CO2 that your travels pumped into the atmosphere.
                        """,
                        List.of("2.47m³", "13,785.3lb=6,252.9kg")),
                Arguments.of("2 months, 18lbs\n 12 months, 100lbs\n", List.of("18.0lb=8.2kg", "100.0lb=45.4kg")));
    }

    @Inject
    UsConversion conversion;

    Set<UsUnitConverter> converters;

    @BeforeEach
    public void setUp() {
        this.converters = Set.of(
                new CalorieConverter(),
                new CupConverter(),
                new FlouidOunceConverter(),
                new FootInchConverter(),
                new GallonConverter(),
                new HorsePowerConverter(),
                new MilesConverter(),
                new PintConverter(),
                new PoundConverter(),
                new TablespoonConverter(),
                new TeapoonConverter(),
                new TemperatureConverter(),
                new WeightOunceConverter());
        final Instance<UsUnitConverter> usUnitConverters = mock(Instance.class);
        when(usUnitConverters.stream()).then(args -> Stream.of(this.converters.toArray()));
        this.conversion = new UsConversion();
        this.conversion.setConverters(usUnitConverters);
    }

    @Test
    public void setUpTestTest() {
        Assertions.assertFalse(
                this.conversion.getConverters().stream().findAny().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("tweetsAndUnits")
    public void testTweet(final String tweet, final List<String> expectedOutputs) {
        final String convertedUnits = this.conversion.returnConverted(tweet);

        LOG.debug("Checking tweet [{}].", tweet);
        LOG.debug("Converted units: [{}]", convertedUnits);

        SoftAssertions softly = new SoftAssertions();
        for (String expectedConversion : expectedOutputs) {
            softly.assertThat(expectedConversion)
                    .as("expected [" + convertedUnits + "] to contain **[" + expectedConversion + "]**.")
                    .withFailMessage("expected [" + convertedUnits + "] to contain **[" + expectedConversion + "]**.")
                    .contains(expectedConversion);
        }
        softly.assertAll();
    }
}
