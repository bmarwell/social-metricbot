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

import static java.util.Arrays.asList;

import io.github.bmarwell.social.metricbot.conversion.DecimalFormats;
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
public class FootInchConverter implements io.github.bmarwell.social.metricbot.conversion.converters.UsUnitConverter {

    private static final long serialVersionUID = 1869416432498279219L;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** 1st matcher is single quotation mark to find numbers. */
    private static final Pattern FOOT_OR_FEET = Pattern.compile(
            "\\b([0-9]+)'(([0-9]{0,2}(\\.[0-9]{0,2})?)(\")?|\\b)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    /**
     * 2nd matcher is using foot|feet|ft find numbers.
     */
    private static final Pattern FOOT_OR_FEET_TEXT = Pattern.compile(
            "\\b([0-9]+,[0-9]{3,}|[0-9]{0,2}|a)\\s*(foot|feet|ft)\\s*(([0-9]{0,2}(\\.[0-9]{0,2})?)(\")?|\\b)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    /**
     * 3rd matcher matches inches only.
     */
    private static final Pattern INCH_ONLY = Pattern.compile(
            // not starting with foot, feet or '.
            // then an optional space
            // then numbers, followed by either ", in, inch or inches.
            "(?<!foot)(?<!feet)(?<!')\\s?(([0-9]+,)?[0-9]+(\\.[0-9]+)?)\\s?(\"|in\\b|inch(es)?)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    /**
     * 4th matcher for literal foot/feet only.
     */
    private static final Pattern FT_TEXT = Pattern.compile(
            "\\b(?<num>\\d+(,\\d+)?|\\d{0,2}|a)\\s*(?:foot|feet|ft)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final NumberFormat numberFormatCm = DecimalFormats.atMostOneFractionDigits();
    private static final NumberFormat numberFormatFt = DecimalFormats.atMostTwoFractionDigits();

    private static final String UNIT_FEET = "'";
    private static final String UNIT_FEET_FT = "ft";
    private static final String UNIT_INCH = "\"";
    private static final String UNIT_CENTIMETERS = "cm";
    private static final String UNIT_METERS = "m";
    private static final String UNIT_KM = "km";

    private static final double CENTIMETERS_PER_FOOT = 30.48;
    private static final double INCHES_PER_FOOT = 12;
    private static final double CENTIMETERS_PER_INCH = CENTIMETERS_PER_FOOT / INCHES_PER_FOOT;

    @Override
    public List<String> getSearchTerms() {
        return asList("foot", "feet");
    }

    @Override
    public boolean matches(final String text) {
        return FOOT_OR_FEET.matcher(text).find()
                || FOOT_OR_FEET_TEXT.matcher(text).find()
                || INCH_ONLY.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        final LinkedHashSet<UnitConversion> outputUnits = new LinkedHashSet<>();
        final Matcher matcherQuotationMark = FOOT_OR_FEET.matcher(text);

        while (matcherQuotationMark.find()) {
            if (matcherQuotationMark.groupCount() < 1) {
                continue;
            }
            final String group = matcherQuotationMark.group(0).replaceAll(",", "");
            footInchToCm(group).ifPresent(outputUnits::add);
        }

        final Matcher matcherTextFt = FOOT_OR_FEET_TEXT.matcher(text);
        while (matcherTextFt.find()) {
            final String feet = matcherTextFt.group(1).replaceAll(",", "");
            final String inches = matcherTextFt.group(4);

            if (feet.isEmpty()) {
                continue;
            }

            footInchTextToCm(feet, inches).ifPresent(outputUnits::add);
        }

        final Matcher matcherInches = INCH_ONLY.matcher(text);
        while (matcherInches.find()) {
            final String inches = matcherInches.group(1).replaceAll(",", "");

            if (inches.isEmpty()) {
                continue;
            }

            inchTextToCm(inches).ifPresent(outputUnits::add);
        }

        final Matcher matcherFeet = FT_TEXT.matcher(text);
        while (matcherFeet.find()) {
            final String inches = matcherFeet.group("num").replaceAll(",", "");

            if (inches.isEmpty()) {
                continue;
            }

            ftTextToCm(inches).ifPresent(outputUnits::add);
        }

        return outputUnits;
    }

    private Optional<UnitConversion> ftTextToCm(final String feet) {
        final double feetDecimal = parseFeet(feet);

        final String inputFeetDecimal = numberFormatFt.format(feetDecimal);

        final double centimetres = feetDecimal * CENTIMETERS_PER_FOOT;
        // usually we use heights up to a man's height in cm, but after that we switch to meters.
        return getUnitConversion(inputFeetDecimal, UNIT_FEET_FT, centimetres);
    }

    private Optional<UnitConversion> inchTextToCm(final String inches) {
        final double inchesDecimal = parseInches(inches);
        final String inchesText = numberFormatFt.format(inchesDecimal);
        final double centimetres = inchesDecimal * CENTIMETERS_PER_INCH;

        return getUnitConversion(inchesText, UNIT_INCH, centimetres);
    }

    private Optional<UnitConversion> footInchTextToCm(final String feet, final String inches) {
        final double feetDecimal = parseFeet(feet);
        final double inchesDecimal = parseInches(inches);

        final double footDecimal = feetDecimal + (inchesDecimal / INCHES_PER_FOOT);

        final String inputFeetDecimal = numberFormatFt.format(footDecimal);

        final double centimetres = footDecimal * CENTIMETERS_PER_FOOT;
        // usually we use heights up to a man's height in cm, but after that we switch to meters.
        return getUnitConversion(inputFeetDecimal, UNIT_FEET, centimetres);
    }

    private Optional<UnitConversion> getUnitConversion(
            final String inputAmount, final String inputUnit, final double centimetres) {
        if (centimetres <= 249) {
            final ImmutableUnitConversion unitConversion = ImmutableUnitConversion.builder()
                    .inputAmount(inputAmount)
                    .inputUnit(inputUnit)
                    .metricAmount(numberFormatCm.format(centimetres))
                    .metricUnit(UNIT_CENTIMETERS)
                    .build();

            return Optional.of(unitConversion);
        }

        final double meters = centimetres / 100;

        if (meters < 10_000.0d) {
            final ImmutableUnitConversion unitConversion = ImmutableUnitConversion.builder()
                    .inputAmount(inputAmount)
                    .inputUnit(inputUnit)
                    .metricAmount(numberFormatCm.format(meters))
                    .metricUnit(UNIT_METERS)
                    .build();

            return Optional.of(unitConversion);
        }

        final double km = meters / 1000;

        final ImmutableUnitConversion unitConversion = ImmutableUnitConversion.builder()
                .inputAmount(inputAmount)
                .inputUnit(inputUnit)
                .metricAmount(numberFormatCm.format(km))
                .metricUnit(UNIT_KM)
                .build();

        return Optional.of(unitConversion);
    }

    private Optional<UnitConversion> footInchToCm(final String group) {
        final String[] footAndInches = group.split("\'");

        if (footAndInches.length > 2) {
            log.info("Can only split foot and inches.");
            return Optional.empty();
        }

        final double feet = parseFeet(footAndInches[0]);
        final double inches = parseInches(footAndInches);
        final double footDecimal = feet + (inches / INCHES_PER_FOOT);

        final String inputFeetDecimal = numberFormatFt.format(footDecimal);

        final double centimetres = footDecimal * CENTIMETERS_PER_FOOT;

        final ImmutableUnitConversion unitConversion = ImmutableUnitConversion.builder()
                .inputAmount(inputFeetDecimal)
                .inputUnit(UNIT_FEET)
                .metricAmount(numberFormatCm.format(centimetres))
                .metricUnit(UNIT_CENTIMETERS)
                .build();
        return Optional.of(unitConversion);
    }

    private double parseInches(final String[] footAndInches) {
        if (footAndInches.length < 2) {
            return 0.0d;
        }

        return parseInches(footAndInches[1]);
    }

    private double parseInches(final /* nullable */ String footAndInches) {
        if (footAndInches == null) {
            return 0.0d;
        }

        final String inches = footAndInches.split("\"", 2)[0];

        if (inches.isEmpty()) {
            return 0.0d;
        }

        try {
            return Double.parseDouble(inches);
        } catch (final NumberFormatException nfe) {
            return 0.0d;
        }
    }

    private double parseFeet(final String footAndInches) {
        if (footAndInches.isEmpty()) {
            return 0.0d;
        }

        if (footAndInches.toLowerCase(Locale.ENGLISH).contains("a")) {
            return 1.0d;
        }

        return Double.parseDouble(footAndInches);
    }
}
