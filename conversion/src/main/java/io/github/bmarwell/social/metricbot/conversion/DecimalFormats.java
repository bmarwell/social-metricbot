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
import java.util.Locale;

public final class DecimalFormats {

    private DecimalFormats() {
        // util.
    }

    public static NumberFormat atMostTwoFractionDigits() {
        final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(2);
        df.setRoundingMode(RoundingMode.HALF_UP);

        return df;
    }

    public static NumberFormat atMostOneFractionDigits() {
        final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(1);
        df.setRoundingMode(RoundingMode.HALF_UP);

        return df;
    }

    public static NumberFormat exactlyOneFractionDigits() {
        final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
        df.setRoundingMode(RoundingMode.HALF_UP);

        return df;
    }

    public static NumberFormat noFractionDigits() {
        final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(0);
        df.setRoundingMode(RoundingMode.HALF_UP);

        return df;
    }
}
