package io.github.bmhm.twitter.metricbot.conversion.converters;

import static java.util.Collections.emptyList;


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.bmhm.twitter.metricbot.conversion.FractionUtil;
import io.github.bmhm.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import io.micronaut.context.annotation.Prototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class CupConverter implements UsUnitConverter {

  private static final Logger LOG = LoggerFactory.getLogger(CupConverter.class);

  private static final Pattern PATTERN_CUPS =
      Pattern.compile("(\\s|\\b)(([0-9]+\\.)?[0-9\\u00BC-\\u00BE\\u2150-\\u215E\\/]+)\\s?cup(s\\b|\\b)",
          Pattern.MULTILINE | Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE);

  private static final String UNIT_CUPS = "C";
  private static final String UNIT_METRIC = "ml";

  private static final double ML_PER_CUP = 236.59d;

  private static final NumberFormat NUMBER_FORMAT_CUPS = createNumberFormatCups();

  private static final NumberFormat NUMBER_FORMAT_MILLIS = createNumberFormatMillis();
  private static final long serialVersionUID = 3577857810056970727L;

  @Override
  public List<String> getSearchTerms() {
    return emptyList();
  }

  @Override
  public boolean matches(final String text) {
    return PATTERN_CUPS.matcher(text).matches();
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
        final String cups = matcher.group(2);
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

  private static NumberFormat createNumberFormatCups() {
    final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);

    df.setMinimumFractionDigits(0);
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.HALF_UP);

    return df;
  }

  private static NumberFormat createNumberFormatMillis() {
    final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);
    df.setMinimumFractionDigits(0);
    df.setMaximumFractionDigits(0);
    df.setRoundingMode(RoundingMode.HALF_UP);

    return df;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "CupConverter{", "}")
        .toString();
  }
}
