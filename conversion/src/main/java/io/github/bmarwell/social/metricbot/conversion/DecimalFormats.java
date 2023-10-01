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
