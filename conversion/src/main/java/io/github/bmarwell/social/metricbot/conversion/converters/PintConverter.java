package io.github.bmarwell.social.metricbot.conversion.converters;

import static java.util.Collections.emptyList;

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
public class PintConverter implements UsUnitConverter {

    private static final Logger LOG = LoggerFactory.getLogger(PintConverter.class);

    private static final Pattern PATTERN_SOURCE =
            Pattern.compile("\\b((?:\\d+\\.)?([\\d,/]+|a))\\s?pt\\b", Pattern.MULTILINE | Pattern.CANON_EQ);
    private static final Pattern PATTERN_SOURCE_FRACTIONS = Pattern.compile(
            "([\u00BC-\u00BE\u2150-\u215E]+|a)\\s?gallon(?:s)?\\b", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final String SYMBOL_SOURCE = "pt";
    private static final String SYMBOL_TARGET = "ml";
    private static final String SYMBOL_TARGET_HIGH = "L";

    private static final double SOURCE_TO_TARGET_MULTIPLICATOR = 473.176_473d;
    private static final double SOURCE_TO_TARGET_MULTIPLICATOR_HIGH = SOURCE_TO_TARGET_MULTIPLICATOR / 1000;

    private static final NumberFormat NUMBER_FORMAT_SOURCE = DecimalFormats.atMostTwoFractionDigits();
    private static final NumberFormat NUMBER_FORMAT_TARGET = DecimalFormats.atMostTwoFractionDigits();
    private static final NumberFormat NUMBER_FORMAT_TARGET_HIGH = DecimalFormats.atMostTwoFractionDigits();

    @Override
    public List<String> getSearchTerms() {
        return emptyList();
    }

    @Override
    public boolean matches(String text) {
        return PATTERN_SOURCE.matcher(text).find()
                || PATTERN_SOURCE_FRACTIONS.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(String text) {

        if (null == text || text.isEmpty()) {
            return emptyList();
        }

        final List<UnitConversion> conversions = new ArrayList<>();
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
        final double gallonsDouble = parseNumberOrWord(gallonsDecimal);

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

    private static UnitConversion getUnitConversion(double sourceAsDouble, String sourceUnitDecimalString) {
        final UnitConversion conversion;
        if (sourceAsDouble >= 2.114d) {
            final var millis = sourceAsDouble * SOURCE_TO_TARGET_MULTIPLICATOR_HIGH;
            final var targetUnitDecimalString = NUMBER_FORMAT_TARGET_HIGH.format(millis);

            conversion = ImmutableUnitConversion.builder()
                    .inputAmount(sourceUnitDecimalString)
                    .inputUnit(SYMBOL_SOURCE)
                    .metricAmount(targetUnitDecimalString)
                    .metricUnit(SYMBOL_TARGET_HIGH)
                    .build();
        } else {
            final var millis = sourceAsDouble * SOURCE_TO_TARGET_MULTIPLICATOR;
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

    private double parseNumberOrWord(String gallonsDecimal) {
        return Double.parseDouble(gallonsDecimal.replaceAll(",", ""));
    }
}
