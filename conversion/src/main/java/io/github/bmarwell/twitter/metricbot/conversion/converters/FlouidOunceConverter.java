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

package io.github.bmarwell.twitter.metricbot.conversion.converters;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import io.github.bmarwell.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmarwell.twitter.metricbot.conversion.UnitConversion;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.Dependent;

@Dependent
public class FlouidOunceConverter implements UsUnitConverter {

    /**
     * Matches 8 fl.oz., 8 fl oz, etc.
     */
    private static final Pattern PATTERN_FL_OZ = Pattern.compile(
            "((\\b|-)?([0-9]+\\.)?[0-9]+) fl(\\.)?( )?oz", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /**
     * Matches 8 oz of water and 8 oz water.
     */
    private static final Pattern PATTERN_OZ_WATER = Pattern.compile(
            "((\\b|-)?([0-9]+\\.)?[0-9]+) oz(\\.)?( of)? (water|oil)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final String UNIT_FL_OZ = "fl.oz";
    private static final String UNIT_ML = "ml";
    private static final double FLOUID_OUNCE_IN_ML = 29.5735295625d;
    private static final long serialVersionUID = -4929469316186219065L;

    @Override
    public List<String> getSearchTerms() {
        return asList("fl. oz.", "fl.oz.");
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN_FL_OZ.matcher(text).find()
                || PATTERN_OZ_WATER.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        final LinkedHashSet<UnitConversion> outputUnits = new LinkedHashSet<>();
        final Matcher matcherFlOz = PATTERN_FL_OZ.matcher(text);
        final Matcher matcherOzWater = PATTERN_OZ_WATER.matcher(text);

        while (matcherFlOz.find()) {
            final double flOz = Double.parseDouble(matcherFlOz.group(1));
            final double ml = Math.round(flOz * FLOUID_OUNCE_IN_ML);

            final UnitConversion unitConversion = ImmutableUnitConversion.builder()
                    .inputAmount("" + flOz)
                    .inputUnit(UNIT_FL_OZ)
                    .metricAmount("" + ml)
                    .metricUnit(UNIT_ML)
                    .build();

            outputUnits.add(unitConversion);
        }

        while (matcherOzWater.find()) {
            final double flOz = Double.parseDouble(matcherOzWater.group(1));
            final double ml = Math.round(flOz * FLOUID_OUNCE_IN_ML);

            final UnitConversion unitConversion = ImmutableUnitConversion.builder()
                    .inputAmount("" + flOz)
                    .inputUnit(UNIT_FL_OZ)
                    .metricAmount("" + ml)
                    .metricUnit(UNIT_ML)
                    .build();

            outputUnits.add(unitConversion);
        }

        return unmodifiableSet(outputUnits);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FlouidOunceConverter.class.getSimpleName() + "[", "]").toString();
    }
}
