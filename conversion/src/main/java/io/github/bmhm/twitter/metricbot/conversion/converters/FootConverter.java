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

  /** 1st matcher is quotation mark to find numbers. */
  private static final Pattern FOOT_OR_FEET =
      Pattern.compile("\\b[0-9]{0,2}\"([0-9]{0,2}|\\b)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);


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
    return FOOT_OR_FEET.matcher(text).find();
  }

  @Override
  public Collection<UnitConversion> getConvertedUnits(final String text) {
    final LinkedHashSet<UnitConversion> outputUnits = new LinkedHashSet<>();
    final Matcher matcherMiles = FOOT_OR_FEET.matcher(text);

    while (matcherMiles.find()) {
      final String group = matcherMiles.group(0);
      footInchToCm(group).ifPresent(outputUnits::add);
    }

    return outputUnits;
  }

  private Optional<UnitConversion> footInchToCm(final String group) {
    final String[] footAndInches = group.split("\"");

    if (footAndInches.length > 2) {
      LOG.info("Can only split foot and inches.");
      return Optional.empty();
    }

    final double feet = parseFeet(footAndInches);
    final double inches = parseInches(footAndInches);
    final double footDecimal = feet + (inches / INCHES_PER_FOOT);

    final NumberFormat df = DecimalFormat.getNumberInstance(Locale.US);
    df.setMinimumFractionDigits(0);
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.HALF_UP);

    final String inputFeetDecimal = df.format(footDecimal);

    final double centimetres = footDecimal * CENTIMETERS_PER_FOOT;

    final ImmutableUnitConversion unitConversion = ImmutableUnitConversion.builder()
        .inputAmount(inputFeetDecimal)
        .inputUnit(UNIT_FEET)
        .metricAmount(df.format(centimetres))
        .metricUnit(UNIT_CENTIMETERS)
        .build();
    return Optional.of(unitConversion);
  }

  private double parseInches(final String[] footAndInches) {
    if (footAndInches.length < 2) {
      return 0.0d;
    }

    return Double.parseDouble(footAndInches[1]);
  }

  private double parseFeet(final String[] footAndInches) {
    return Double.parseDouble(footAndInches[0]);
  }
}
