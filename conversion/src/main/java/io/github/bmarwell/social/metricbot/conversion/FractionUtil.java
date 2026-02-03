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
package io.github.bmarwell.social.metricbot.conversion;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FractionUtil {

    private static final Map<String, String> FRACTION_DICTIONARY = createFractionDictionary();

    private static final Pattern PATTERN_FRACTION = Pattern.compile("\\b([0-9]+)/([0-9]+)\\b", Pattern.MULTILINE);

    private static final NumberFormat NUMBERFORMAT_OUT = createNumberFormat();

    private FractionUtil() {
        // util class.
    }

    private static NumberFormat createNumberFormat() {
        final NumberFormat numberFormat = DecimalFormat.getInstance(Locale.ENGLISH);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);

        return numberFormat;
    }

    private static Map<String, String> createFractionDictionary() {
        final ConcurrentHashMap<String, String> dict = new ConcurrentHashMap<>();
        dict.put("¼", "0.25");
        dict.put("½", "0.5");
        dict.put("¾", "0.75");
        dict.put("⅐", "0.14285714285");
        dict.put("⅑", "0.11111111111");
        dict.put("⅒", "0.1");
        dict.put("⅓", "0.333333333333");
        dict.put("⅔", "0.666666666667");
        dict.put("⅕", "0.2");
        dict.put("⅖", "0.4");
        dict.put("⅗", "0.6");
        dict.put("⅘", "0.8");
        dict.put("⅙", "0.16666666666");
        dict.put("⅚", "0.83333333333");
        dict.put("⅛", "0.125");
        dict.put("⅜", "0.375");
        dict.put("⅝", "0.625");
        dict.put("⅞", "0.875");

        return Collections.unmodifiableMap(dict);
    }

    public static String replaceFractions(final String input) {
        final AtomicReference<String> stringRef = new AtomicReference<>(input);
        FRACTION_DICTIONARY.forEach((key, value) -> stringRef.getAndUpdate(old -> old.replace(key, value)));

        // try to convert fractions.
        Matcher matcher;
        while ((matcher = PATTERN_FRACTION.matcher(stringRef.get())).find()) {
            final String divident = matcher.group(1);
            final String divisor = matcher.group(2);
            stringRef.getAndUpdate(old -> replaceNonUnicodeFractions(old, divident, divisor));
        }

        return stringRef.get();
    }

    private static String replaceNonUnicodeFractions(
            final String old, final String dividentStr, final String divisorStr) {
        try {
            final double divident = Double.parseDouble(dividentStr.trim());
            final double divisor = Double.parseDouble(divisorStr.trim());

            final String decimalString = NUMBERFORMAT_OUT.format(divident / divisor);

            return old.replace(dividentStr + "/" + divisorStr, decimalString);
        } catch (final NumberFormatException | ArithmeticException nfe) {
            // Unable to calculate fraction, return original string
        }

        return old;
    }
}
