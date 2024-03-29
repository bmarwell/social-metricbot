/*
 * Copyright 2019-2023 The social-metricbot contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bmarwell.social.metricbot.conversion.converters;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import io.github.bmarwell.social.metricbot.conversion.DecimalFormats;
import io.github.bmarwell.social.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmarwell.social.metricbot.conversion.UnitConversion;
import jakarta.enterprise.context.Dependent;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Dependent
public class PoundConverter implements UsUnitConverter {

    private static final Pattern PATTERN_LB = Pattern.compile(
            "\\b((?:(\\d|,)+\\.)?([\\d,/]+|a))\\s?(lb|pound)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final double GRAMS_PER_KG = 0.453_592_37d;
    private static final String UNIT_POUND = "lb";
    private static final String UNIT_METRIC = "kg";

    private static final long serialVersionUID = 1L;
    private static final NumberFormat NUMBER_FORMAT_ONE_FRACTION_DIGITS = DecimalFormats.exactlyOneFractionDigits();

    public PoundConverter() {
        // injection.
    }

    @Override
    public List<String> getSearchTerms() {
        return asList("lb", "pounds");
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN_LB.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        final Matcher matcher = PATTERN_LB.matcher(text);
        final LinkedHashSet<UnitConversion> outputUnits = new LinkedHashSet<>();

        while (matcher.find()) {
            final double ounces = Double.parseDouble(matcher.group(1).replaceAll(",", ""));
            final String grams = NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(ounces * GRAMS_PER_KG);

            final UnitConversion unitConversion = ImmutableUnitConversion.builder()
                    .inputAmount(NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(ounces))
                    .inputUnit(UNIT_POUND)
                    .metricAmount(grams)
                    .metricUnit(UNIT_METRIC)
                    .build();

            outputUnits.add(unitConversion);
        }

        return unmodifiableSet(outputUnits);
    }
}
