/*
 * Copyright 2020-2023 The social-metricbot contributors
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

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import io.github.bmarwell.social.metricbot.conversion.DecimalFormats;
import io.github.bmarwell.social.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmarwell.social.metricbot.conversion.UnitConversion;
import jakarta.enterprise.context.Dependent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class CalorieConverter implements io.github.bmarwell.social.metricbot.conversion.converters.UsUnitConverter {

    private static final long serialVersionUID = 4539101040460638085L;

    private static final Logger LOG = LoggerFactory.getLogger(CalorieConverter.class);

    private static final Pattern PATTERN_CALORIES = Pattern.compile(
            "\\b((?:[0-9]+,)?[0-9]+)\\s?cal(?:s|orie(?:s)?)?\\b", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final double KILOJOULE_PER_CALORIE = 4.184d;

    private static final NumberFormat NUMBER_FORMAT_CALS = DecimalFormats.noFractionDigits();

    @Override
    public List<String> getSearchTerms() {
        return emptyList();
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN_CALORIES.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        if (text == null) {
            return emptyList();
        }

        if (text.isEmpty()) {
            return emptyList();
        }

        final List<UnitConversion> convertedUnits = new ArrayList<>();

        final Matcher matcher = PATTERN_CALORIES.matcher(text);
        while (matcher.find()) {
            try {
                final String cals = matcher.group(1).replaceAll(",", "");
                final double calsDecimal = Double.parseDouble(cals);
                final double kj = calsDecimal * KILOJOULE_PER_CALORIE;

                final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
                        .inputAmount(NUMBER_FORMAT_CALS.format(calsDecimal))
                        .inputUnit("cal")
                        .metricAmount(NUMBER_FORMAT_CALS.format(kj))
                        .metricUnit("kJ")
                        .build();

                convertedUnits.add(conversion);
            } catch (final NumberFormatException | ArithmeticException nfe) {
                LOG.error("Unable to convert text[{}].", text);
            }
        }

        return unmodifiableList(convertedUnits);
    }
}
