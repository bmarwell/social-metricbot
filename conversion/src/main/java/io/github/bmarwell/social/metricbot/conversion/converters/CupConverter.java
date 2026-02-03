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
import io.github.bmarwell.social.metricbot.conversion.FractionUtil;
import io.github.bmarwell.social.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmarwell.social.metricbot.conversion.UnitConversion;
import jakarta.enterprise.context.Dependent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class CupConverter implements io.github.bmarwell.social.metricbot.conversion.converters.UsUnitConverter {

    private static final long serialVersionUID = 3577857810056970727L;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Pattern PATTERN_CUPS = Pattern.compile(
            "\\b((?:[0-9]+\\.)?[0-9/]+)\\s?cup(?:s)?\\b",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

    private static final Pattern PATTERN_CUPS_FRACTIONS = Pattern.compile(
            "([\u00BC-\u00BE\u2150-\u215E]+)\\s?cup(?:s)?\\b", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final String UNIT_CUPS = "C";
    private static final String UNIT_METRIC = "ml";

    private static final double ML_PER_CUP = 236.59d;

    private static final NumberFormat NUMBER_FORMAT_CUPS = DecimalFormats.atMostTwoFractionDigits();
    private static final NumberFormat NUMBER_FORMAT_MILLIS = DecimalFormats.noFractionDigits();

    @Override
    public List<String> getSearchTerms() {
        return emptyList();
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN_CUPS.matcher(text).find()
                || PATTERN_CUPS_FRACTIONS.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        if (null == text || text.isEmpty()) {
            return emptyList();
        }

        final List<UnitConversion> conversions = new ArrayList<>();
        final Matcher matcher = PATTERN_CUPS.matcher(text);

        while (matcher.find()) {
            try {
                final String cups = matcher.group(1);
                final String cupsDecimal = FractionUtil.replaceFractions(cups);
                final double cupsDouble = Double.parseDouble(cupsDecimal);

                final double millis = cupsDouble * ML_PER_CUP;

                final String cupsDecimalString = NUMBER_FORMAT_CUPS.format(cupsDouble);
                final String millisDecimalString = NUMBER_FORMAT_MILLIS.format(millis);
                log.debug("Converted [{}]C to [{}]ml.", cupsDecimalString, millisDecimalString);

                final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
                        .inputAmount(cupsDecimalString)
                        .inputUnit(UNIT_CUPS)
                        .metricAmount(millisDecimalString)
                        .metricUnit(UNIT_METRIC)
                        .build();

                conversions.add(conversion);
            } catch (final NumberFormatException | ArithmeticException nfe) {
                log.error("Unable to convert [{}].", text, nfe);
            }
        }

        final Matcher fractionMatcher = PATTERN_CUPS_FRACTIONS.matcher(text);
        while (fractionMatcher.find()) {
            try {
                final String cups = fractionMatcher.group(1);
                final String cupsDecimal = FractionUtil.replaceFractions(cups);
                final double cupsDouble = Double.parseDouble(cupsDecimal);

                final double millis = cupsDouble * ML_PER_CUP;

                final String cupsDecimalString = NUMBER_FORMAT_CUPS.format(cupsDouble);
                final String millisDecimalString = NUMBER_FORMAT_MILLIS.format(millis);
                log.debug("Converted [{}]C to [{}]ml.", cupsDecimalString, millisDecimalString);

                final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
                        .inputAmount(cupsDecimalString)
                        .inputUnit(UNIT_CUPS)
                        .metricAmount(millisDecimalString)
                        .metricUnit(UNIT_METRIC)
                        .build();

                conversions.add(conversion);
            } catch (final NumberFormatException | ArithmeticException nfe) {
                log.error("Unable to convert [{}].", text, nfe);
            }
        }

        return unmodifiableList(conversions);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "CupConverter{", "}").toString();
    }
}
