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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.Dependent;

@Dependent
public class WeightOunceConverter implements UsUnitConverter {

    /**
     * Matches 8 fl.oz., 8 fl oz, etc.
     */
    private static final Pattern PATTERN_OZ = Pattern.compile(
            "((\\b|[^0-9]-)?([0-9]+\\.)?[0-9]+)( )?(oz|ounce)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final double GRAMS_PER_OUNCE = 28.349_523_125d;
    private static final String UNIT_OUNCE = "oz";
    private static final String UNIT_GRAM = "g";
    private static final long serialVersionUID = -4835657553076763553L;
    private static final NumberFormat NUMBER_FORMAT_ONE_FRACTION_DIGITS = DecimalFormats.exactlyOneFractionDigits();

    public WeightOunceConverter() {
        // injection.
    }

    @Override
    public List<String> getSearchTerms() {
        return asList("ounce", "ounces");
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN_OZ.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        final Matcher matcher = PATTERN_OZ.matcher(text);
        final LinkedHashSet<UnitConversion> outputUnits = new LinkedHashSet<>();

        while (matcher.find()) {
            final double ounces = Double.parseDouble(matcher.group(1));
            final String grams = NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(ounces * GRAMS_PER_OUNCE);

            final UnitConversion unitConversion = ImmutableUnitConversion.builder()
                    .inputAmount(NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(ounces))
                    .inputUnit(UNIT_OUNCE)
                    .metricAmount(grams)
                    .metricUnit(UNIT_GRAM)
                    .build();

            outputUnits.add(unitConversion);
        }

        return unmodifiableSet(outputUnits);
    }
}
