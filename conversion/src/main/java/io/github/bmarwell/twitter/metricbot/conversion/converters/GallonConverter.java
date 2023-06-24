package io.github.bmarwell.twitter.metricbot.conversion.converters;

import static java.util.Collections.emptyList;

import io.github.bmarwell.twitter.metricbot.conversion.DecimalFormats;
import io.github.bmarwell.twitter.metricbot.conversion.FractionUtil;
import io.github.bmarwell.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmarwell.twitter.metricbot.conversion.UnitConversion;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class GallonConverter implements UsUnitConverter {

    private static final long serialVersionUID = 3577857810056970727L;

    private static final Logger LOG = LoggerFactory.getLogger(GallonConverter.class);

    private static final Pattern PATTERN_SOURCE = Pattern.compile(
            "\\b((?:\\d+\\.)?([\\d/]+|a))\\s?(mil(lion(s)?)? )?gallon(?:s)?\\b",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

    private static final Pattern PATTERN_SOURCE_FRACTIONS = Pattern.compile(
            "([\u00BC-\u00BE\u2150-\u215E]+|a)\\s?gallon(?:s)?\\b", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final String SYMBOL_SOURCE = "gal";
    private static final String SYMBOL_TARGET = "L";

    private static final double SOURCE_TO_TARGET_MULTIPLICATOR = 3.785_41d;

    private static final NumberFormat NUMBER_FORMAT_SOURCE = DecimalFormats.atMostTwoFractionDigits();
    private static final NumberFormat NUMBER_FORMAT_TARGET = DecimalFormats.atMostTwoFractionDigits();

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

        final LinkedList<UnitConversion> conversions = new LinkedList<>();
        final Matcher matcher = PATTERN_SOURCE.matcher(text);

        parseUnitOccurences(text, conversions, matcher);

        final Matcher fractionMatcher = PATTERN_SOURCE_FRACTIONS.matcher(text);
        parseUnitOccurences(text, conversions, fractionMatcher);

        return conversions;
    }

    private void parseUnitOccurences(String text, LinkedList<UnitConversion> conversions, Matcher matcher) {
        while (matcher.find()) {
            try {
                parseUnitOccurence(conversions, matcher);
            } catch (final NumberFormatException | ArithmeticException nfe) {
                LOG.error("Unable to convert [{}].", text, nfe);
            }
        }
    }

    private void parseUnitOccurence(LinkedList<UnitConversion> conversions, Matcher matcher) {
        final String source = matcher.group(1);
        final String gallonsDecimal = FractionUtil.replaceFractions(source);
        final double gallonsDouble = parseNumberOrWord(gallonsDecimal);

        final double millis = gallonsDouble * SOURCE_TO_TARGET_MULTIPLICATOR;

        final String sourceUnitDecimalString = NUMBER_FORMAT_SOURCE.format(gallonsDouble);
        final String targetUnitDecimalString = NUMBER_FORMAT_TARGET.format(millis);
        LOG.debug(
                "Converted [{}]{} to [{}]{}.",
                sourceUnitDecimalString,
                SYMBOL_SOURCE,
                targetUnitDecimalString,
                SYMBOL_TARGET);

        final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
                .inputAmount(sourceUnitDecimalString)
                .inputUnit(SYMBOL_SOURCE)
                .metricAmount(targetUnitDecimalString)
                .metricUnit(SYMBOL_TARGET)
                .build();

        conversions.add(conversion);
    }

    private double parseNumberOrWord(String gallonsDecimal) {
        if ("a".equals(gallonsDecimal)) {
            return 1.0d;
        }

        return Double.parseDouble(gallonsDecimal);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GallonConverter.class.getSimpleName() + "[", "]").toString();
    }
}
