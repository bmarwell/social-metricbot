package io.github.bmhm.twitter.metricbot.conversion.converters;

import static java.util.Collections.emptyList;

import io.github.bmhm.twitter.metricbot.conversion.DecimalFormats;
import io.github.bmhm.twitter.metricbot.conversion.FractionUtil;
import io.github.bmhm.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
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
public class CupConverter implements UsUnitConverter {

  private static final long serialVersionUID = 3577857810056970727L;

  private static final Logger LOG = LoggerFactory.getLogger(CupConverter.class);

  private static final Pattern PATTERN_CUPS =
      Pattern.compile("\\b((?:[0-9]+\\.)?[0-9/]+)\\s?cup(?:s)?\\b",
          Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

  private static final Pattern PATTERN_CUPS_FRACTIONS =
      Pattern.compile("([\u00BC-\u00BE\u2150-\u215E]+)\\s?cup(?:s)?\\b",
          Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

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

    final LinkedList<UnitConversion> conversions = new LinkedList<>();
    final Matcher matcher = PATTERN_CUPS.matcher(text);

    while (matcher.find()) {
      try {
        final String cups = matcher.group(1);
        final String cupsDecimal = FractionUtil.replaceFractions(cups);
        final double cupsDouble = Double.parseDouble(cupsDecimal);

        final double millis = cupsDouble * ML_PER_CUP;

        final String cupsDecimalString = NUMBER_FORMAT_CUPS.format(cupsDouble);
        final String millisDecimalString = NUMBER_FORMAT_MILLIS.format(millis);
        LOG.debug("Converted [{}]C to [{}]ml.", cupsDecimalString, millisDecimalString);

        final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
            .inputAmount(cupsDecimalString)
            .inputUnit(UNIT_CUPS)
            .metricAmount(millisDecimalString)
            .metricUnit(UNIT_METRIC)
            .build();

        conversions.add(conversion);
      } catch (final NumberFormatException | ArithmeticException nfe) {
        LOG.error("Unable to convert [{}].", text, nfe);
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
        LOG.debug("Converted [{}]C to [{}]ml.", cupsDecimalString, millisDecimalString);

        final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
            .inputAmount(cupsDecimalString)
            .inputUnit(UNIT_CUPS)
            .metricAmount(millisDecimalString)
            .metricUnit(UNIT_METRIC)
            .build();

        conversions.add(conversion);
      } catch (final NumberFormatException | ArithmeticException nfe) {
        LOG.error("Unable to convert [{}].", text, nfe);
      }
    }

    return conversions;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "CupConverter{", "}")
        .toString();
  }
}
