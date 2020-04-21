package io.github.bmhm.twitter.metricbot.conversion.converters;

import static java.util.Arrays.asList;


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.bmhm.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import io.micronaut.context.annotation.Prototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class FootConverter implements UsUnitConverter {

  private static final Logger LOG = LoggerFactory.getLogger(FootConverter.class);

  /** 1st matcher is single quotation mark to find numbers. */
  private static final Pattern FOOT_OR_FEET =
      Pattern.compile("\\b[0-9]{0,2}'(([0-9]{0,2}(\\.[0-9]{0,2})?)(\")?|\\b)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
  /**
   * 2nd matcher is using foot|feet|ft find numbers.
   */
  private static final Pattern FOOT_OR_FEET_TEXT =
      Pattern.compile("\\b([0-9]{0,2}|a)\\s*(foot|feet|ft)\\s*(([0-9]{0,2}(\\.[0-9]{0,2})?)(\")?|\\b)",
          Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

  private static final NumberFormat numberFormatCm = createNumberFormatCm();
  private static final NumberFormat numberFormatFt = createNumberFormatFt();

  private static final String UNIT_FEET = "ft";
  private static final String UNIT_CENTIMETERS = "cm";
  private static final double CENTIMETERS_PER_FOOT = 30.48;
  private static final double INCHES_PER_FOOT = 12;


  @Override
  public List<String> getSearchTerms() {
    return asList("foot", "feet");
  }

  @Override
  public boolean matches(final String text) {
    return FOOT_OR_FEET.matcher(text).find() || FOOT_OR_FEET_TEXT.matcher(text).find();
  }

  @Override
  public Collection<UnitConversion> getConvertedUnits(final String text) {
    final LinkedHashSet<UnitConversion> outputUnits = new LinkedHashSet<>();
    final Matcher matcherQuotationMark = FOOT_OR_FEET.matcher(text);

    while (matcherQuotationMark.find()) {
      final String group = matcherQuotationMark.group(0);
      footInchToCm(group).ifPresent(outputUnits::add);
    }

    final Matcher matcherTextFt = FOOT_OR_FEET_TEXT.matcher(text);
    while (matcherTextFt.find()) {
      final String feet = matcherTextFt.group(1);
      final String inches = matcherTextFt.group(4);

      if (feet.isEmpty()) {
        continue;
      }

      footInchTextToCm(feet, inches).ifPresent(outputUnits::add);
    }

    return outputUnits;
  }

  private Optional<UnitConversion> footInchTextToCm(final String feet, final String inches) {
    final double feetDecimal = parseFeet(feet);
    final double inchesDecimal = parseInches(inches);

    final double footDecimal = feetDecimal + (inchesDecimal / INCHES_PER_FOOT);

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

  private Optional<UnitConversion> footInchToCm(final String group) {
    final String[] footAndInches = group.split("\'");

    if (footAndInches.length > 2) {
      LOG.info("Can only split foot and inches.");
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

  private static NumberFormat createNumberFormatCm() {
    final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);

    df.setMinimumFractionDigits(0);
    df.setMaximumFractionDigits(1);
    df.setRoundingMode(RoundingMode.HALF_UP);

    return df;
  }

  private static NumberFormat createNumberFormatFt() {
    final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);

    df.setMinimumFractionDigits(0);
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.HALF_UP);

    return df;
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

    return Double.parseDouble(inches);
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
