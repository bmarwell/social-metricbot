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

import io.github.bmarwell.twitter.metricbot.conversion.DecimalFormats;
import io.github.bmarwell.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmarwell.twitter.metricbot.conversion.NumberNames;
import io.github.bmarwell.twitter.metricbot.conversion.UnitConversion;
import jakarta.enterprise.context.Dependent;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class MilesConverter implements UsUnitConverter {

    private static final long serialVersionUID = 2837876326659237432L;

    private static final Logger LOG = LoggerFactory.getLogger(MilesConverter.class);

    private static final Pattern MILES = Pattern.compile(
            "((\\b|[^0-9]-)?([0-9]+,){0,4}([0-9]+\\.)?[0-9]+|a)(\\s)?(mi(le(s)?)?\\b)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final Pattern MILES_K = Pattern.compile(
            "((?:\\b|[^0-9]-)?([0-9]+,){0,4}[0-9]+k)(\\s)?(?:mi(?:le(?:s)?)?\\b)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final Pattern MILES_WORD =
            Pattern.compile("\\b(" + String.join("|", NumberNames.ALL) + ")(?:[-]|\\s)?(?:mi(?:le(?:s)?)?)\\b");

    private static final String UNIT_MILES = "mi";
    private static final String UNIT_KM = "km";
    private static final double MILES_IN_METERS = 1609.344;

    private static final NumberFormat NUMBER_FORMAT_ONE_FRACTION_DIGITS = DecimalFormats.atMostOneFractionDigits();

    @Override
    public List<String> getSearchTerms() {
        return asList("miles");
    }

    @Override
    public boolean matches(final String text) {
        return MILES.matcher(text).find()
                || MILES_K.matcher(text).find()
                || MILES_WORD.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        final LinkedHashSet<UnitConversion> outputUnits = new LinkedHashSet<>();

        final Matcher matcherMiles = MILES.matcher(text);
        while (matcherMiles.find()) {
            try {
                final String group = matcherMiles.group(1).replaceAll(",", "");
                final double miles = getMilesFromRegexCapture(group);
                final double meters = miles * MILES_IN_METERS;
                final String km = NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(meters / 1000);

                final UnitConversion unitConversion = ImmutableUnitConversion.builder()
                        .inputAmount(NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(miles))
                        .inputUnit(UNIT_MILES)
                        .metricAmount(km)
                        .metricUnit(UNIT_KM)
                        .build();

                outputUnits.add(unitConversion);
            } catch (final NumberFormatException | ArithmeticException nfEx) {
                LOG.error("Unable to convert [{}].", text, nfEx);
            }
        }

        final Matcher matcherMilesK = MILES_K.matcher(text);
        while (matcherMilesK.find()) {
            try {
                final String group = matcherMilesK
                        .group(1)
                        .toLowerCase(Locale.ENGLISH)
                        .replaceAll(",", "")
                        .replaceAll("k", "000");
                final double miles = getMilesFromRegexCapture(group);
                final double meters = miles * MILES_IN_METERS;
                final String km = NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(meters / 1000);

                final UnitConversion unitConversion = ImmutableUnitConversion.builder()
                        .inputAmount(NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(miles))
                        .inputUnit(UNIT_MILES)
                        .metricAmount(km)
                        .metricUnit(UNIT_KM)
                        .build();

                outputUnits.add(unitConversion);
            } catch (final NumberFormatException | ArithmeticException nfEx) {
                LOG.error("Unable to convert [{}].", text, nfEx);
            }
        }

        final Matcher matcherMilesWord = MILES_WORD.matcher(text);
        while (matcherMilesWord.find()) {
            try {
                final String group =
                        matcherMilesWord.group(1).toLowerCase(Locale.ENGLISH).replaceAll(",", "");
                final Optional<Double> milesDecimalOpt = NumberNames.convert(group);
                if (milesDecimalOpt.isEmpty()) {
                    LOG.info("Unable to convert [{}] from [{}].", group, text);
                    continue;
                }

                final Double milesDecimal = milesDecimalOpt.orElseThrow();
                final double meters = milesDecimal * MILES_IN_METERS;
                final String km = NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(meters / 1000);

                final UnitConversion unitConversion = ImmutableUnitConversion.builder()
                        .inputAmount(NUMBER_FORMAT_ONE_FRACTION_DIGITS.format(milesDecimal))
                        .inputUnit(UNIT_MILES)
                        .metricAmount(km)
                        .metricUnit(UNIT_KM)
                        .build();

                outputUnits.add(unitConversion);
            } catch (final NumberFormatException | ArithmeticException nfEx) {
                LOG.error("Unable to convert [{}].", text, nfEx);
            }
        }

        return unmodifiableSet(outputUnits);
    }

    private static double getMilesFromRegexCapture(String group) {
        if ("a".equals(group)) {
            return 1.0;
        }

        return Double.parseDouble(group);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MilesConverter.class.getSimpleName() + "[", "]").toString();
    }
}
