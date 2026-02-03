/*
 * Copyright 2023 The social-metricbot contributors
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

import io.github.bmarwell.social.metricbot.conversion.DecimalFormats;
import io.github.bmarwell.social.metricbot.conversion.FractionUtil;
import io.github.bmarwell.social.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmarwell.social.metricbot.conversion.UnitConversion;
import jakarta.enterprise.context.Dependent;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class GallonConverter implements io.github.bmarwell.social.metricbot.conversion.converters.UsUnitConverter {

    private static final long serialVersionUID = 3577857810056970727L;

    private static final Logger LOG = LoggerFactory.getLogger(GallonConverter.class);

    private static final Pattern PATTERN_SOURCE = Pattern.compile(
            "\\b((?:\\d+\\.)?([\\d,/]+|a))\\s?(?<exp>(mil|bil)(lion(s)?)? )?gallon(?:s)?\\b",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

    private static final Pattern PATTERN_SOURCE_FRACTIONS = Pattern.compile(
            "([\u00BC-\u00BE\u2150-\u215E]+|a)\\s?gallon(?:s)?\\b", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final String SYMBOL_SOURCE = "gal";
    private static final String SYMBOL_TARGET = "L";
    private static final String SYMBOL_TARGET_HIGH = "m³";

    private static final double SOURCE_TO_TARGET_MULTIPLICATOR = 3.785_41d;
    private static final double SOURCE_TO_TARGET_MULTIPLICATOR_HIGH = 0.003_785_411_795_401_118_5d;

    private static final NumberFormat NUMBER_FORMAT_SOURCE = DecimalFormats.atMostTwoFractionDigits();
    private static final NumberFormat NUMBER_FORMAT_TARGET = DecimalFormats.atMostTwoFractionDigits();
    private static final NumberFormat NUMBER_FORMAT_TARGET_HIGH = DecimalFormats.atMostTwoFractionDigits();

    @Override
    public List<String> getSearchTerms() {
        return emptyList();
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN_SOURCE.matcher(text).find()
                || PATTERN_SOURCE_FRACTIONS.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        if (null == text || text.isEmpty()) {
            return emptyList();
        }

        final ArrayList<UnitConversion> conversions = new ArrayList<>();
        final Matcher matcher = PATTERN_SOURCE.matcher(text);

        parseUnitOccurences(text, conversions, matcher);

        final Matcher fractionMatcher = PATTERN_SOURCE_FRACTIONS.matcher(text);
        parseUnitOccurences(text, conversions, fractionMatcher);

        return List.copyOf(conversions);
    }

    private void parseUnitOccurences(String text, List<UnitConversion> conversions, Matcher matcher) {
        while (matcher.find()) {
            try {
                parseUnitOccurence(conversions, matcher);
            } catch (final NumberFormatException | ArithmeticException nfe) {
                LOG.error("Unable to convert [{}].", text, nfe);
            }
        }
    }

    private void parseUnitOccurence(List<UnitConversion> conversions, Matcher matcher) {
        final String source = matcher.group(1);
        final String gallonsDecimal = FractionUtil.replaceFractions(source);
        final double gallonsDouble = parseNumberOrWord(gallonsDecimal, matcher);

        final String sourceUnitDecimalString = NUMBER_FORMAT_SOURCE.format(gallonsDouble);

        final UnitConversion conversion = getUnitConversion(gallonsDouble, sourceUnitDecimalString);

        LOG.debug(
                "Converted [{}]{} to [{}]{}.",
                sourceUnitDecimalString,
                SYMBOL_SOURCE,
                conversion.getMetricAmount(),
                conversion.getMetricUnit());

        conversions.add(conversion);
    }

    private static UnitConversion getUnitConversion(double gallonsDouble, String sourceUnitDecimalString) {
        final UnitConversion conversion;
        if (gallonsDouble > 265) {
            final var millis = gallonsDouble * SOURCE_TO_TARGET_MULTIPLICATOR_HIGH;
            final var targetUnitDecimalString = NUMBER_FORMAT_TARGET_HIGH.format(millis);

            conversion = ImmutableUnitConversion.builder()
                    .inputAmount(sourceUnitDecimalString)
                    .inputUnit(SYMBOL_SOURCE)
                    .metricAmount(targetUnitDecimalString)
                    .metricUnit(SYMBOL_TARGET_HIGH)
                    .build();
        } else {
            final var millis = gallonsDouble * SOURCE_TO_TARGET_MULTIPLICATOR;
            final var targetUnitDecimalString = NUMBER_FORMAT_TARGET.format(millis);

            conversion = ImmutableUnitConversion.builder()
                    .inputAmount(sourceUnitDecimalString)
                    .inputUnit(SYMBOL_SOURCE)
                    .metricAmount(targetUnitDecimalString)
                    .metricUnit(SYMBOL_TARGET)
                    .build();
        }
        return conversion;
    }

    private double parseNumberOrWord(String gallonsDecimal, Matcher matcherForExp) {
        final Optional<String> exp = getExponent(matcherForExp);

        final double multiplicator =
                exp.stream().mapToDouble(this::strToMultiplicator).findFirst().orElse(1.0d);

        if ("a".equals(gallonsDecimal)) {
            return multiplicator;
        }

        return multiplicator * Double.parseDouble(gallonsDecimal.replaceAll(",", ""));
    }

    private double strToMultiplicator(String exp) {
        if (exp.toLowerCase(Locale.ROOT).startsWith("mil")) {
            return 1_000_000d;
        } else if (exp.toLowerCase(Locale.ROOT).startsWith("bil")) {
            return 1_000_000_000d;
        } else if (exp.toLowerCase(Locale.ROOT).startsWith("tril")) {
            return 1_000_000_000_000d;
        } else {
            LOG.error("unknown multiplicator: " + exp.toLowerCase(Locale.ROOT));
            return 1d;
        }
    }

    private static Optional<String> getExponent(Matcher matcherForExp) {
        try {
            return Optional.ofNullable(matcherForExp.group("exp"));
        } catch (IllegalArgumentException iae) {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GallonConverter.class.getSimpleName() + "[", "]").toString();
    }
}
