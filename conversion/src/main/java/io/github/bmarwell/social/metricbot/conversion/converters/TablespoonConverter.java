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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class TablespoonConverter implements io.github.bmarwell.social.metricbot.conversion.converters.UsUnitConverter {

    private static final long serialVersionUID = -3692749135515082850L;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Pattern PATTERN_TBSP = Pattern.compile(
            "\\b((?:[0-9]+,)?(?:[0-9]+\\.)?[0-9/]+)\\s?(?:tbsp|tablespoon)(?:s)?\\b",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_TBSP_FRAC = Pattern.compile(
            "([\u00BC-\u00BE\u2150-\u215E]+)\\s?(?:tbsp|tablespoon)(?:s)?\\b",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final double GRAMS_PER_TBSP = 15;

    private static final NumberFormat TBSP_NUMBER_FORMAT = DecimalFormats.atMostTwoFractionDigits();
    private static final NumberFormat GRAM_NUMBER_FORMAT = DecimalFormats.noFractionDigits();

    @Override
    public List<String> getSearchTerms() {
        return List.of("tbsp");
    }

    @Override
    public boolean matches(final String text) {
        if (null == text || text.isEmpty()) {
            return false;
        }

        return PATTERN_TBSP.matcher(text).find()
                || PATTERN_TBSP_FRAC.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        if (text == null || text.isEmpty()) {
            return emptyList();
        }

        final List<UnitConversion> conversions = new ArrayList<>();

        final Matcher matcher = PATTERN_TBSP.matcher(text);
        while (matcher.find()) {
            try {
                final String tbspText = matcher.group(1).replaceAll(",", "");
                final String tbspTextDecimal = FractionUtil.replaceFractions(tbspText);
                final double tbspDecimal = Double.parseDouble(tbspTextDecimal);

                final double grams = tbspDecimal * GRAMS_PER_TBSP;

                final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
                        .inputAmount(TBSP_NUMBER_FORMAT.format(tbspDecimal))
                        .inputUnit("tbsp")
                        .metricAmount(GRAM_NUMBER_FORMAT.format(grams))
                        .metricUnit("g")
                        .build();

                conversions.add(conversion);
            } catch (final NumberFormatException | ArithmeticException nfe) {
                log.error("Unable to parse: [{}].", text, nfe);
            }
        }

        final Matcher matcherFrac = PATTERN_TBSP_FRAC.matcher(text);
        while (matcherFrac.find()) {
            try {
                final String tbspText = matcherFrac.group(1).replaceAll(",", "");
                final String tbspTextDecimal = FractionUtil.replaceFractions(tbspText);
                final double tbspDecimal = Double.parseDouble(tbspTextDecimal);

                final double grams = tbspDecimal * GRAMS_PER_TBSP;

                final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
                        .inputAmount(TBSP_NUMBER_FORMAT.format(tbspDecimal))
                        .inputUnit("tbsp")
                        .metricAmount(GRAM_NUMBER_FORMAT.format(grams))
                        .metricUnit("g")
                        .build();

                conversions.add(conversion);
            } catch (final NumberFormatException | ArithmeticException nfe) {
                log.error("Unable to parse: [{}].", text, nfe);
            }
        }

        return unmodifiableList(conversions);
    }
}
