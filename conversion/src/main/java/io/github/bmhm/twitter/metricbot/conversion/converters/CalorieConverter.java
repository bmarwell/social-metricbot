package io.github.bmhm.twitter.metricbot.conversion.converters;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.bmhm.twitter.metricbot.conversion.DecimalFormats;
import io.github.bmhm.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import io.micronaut.context.annotation.Prototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class CalorieConverter implements UsUnitConverter {

  private static final long serialVersionUID = 4539101040460638085L;

  private static final Logger LOG = LoggerFactory.getLogger(CalorieConverter.class);

  private static final Pattern PATTERN_CALORIES = Pattern.compile(
      "\\b((?:[0-9]+,)?[0-9]+)\\s?cal(?:s|orie(?:s)?)?\\b",
      Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

  private static final double KILOJOULE_PER_CALORIE = 4.184d;

  private static final NumberFormat NUMBER_FORMAT_CALS = DecimalFormats.noFractionDigits();


  @Override
  public List<String> getSearchTerms() {
    return emptyList();
  }

  @Override
  public boolean matches(final String text) {
    return PATTERN_CALORIES.matcher(text).find();
  }

  @Override
  public Collection<UnitConversion> getConvertedUnits(final String text) {
    if (text == null) {
      return emptyList();
    }

    if (text.isEmpty()) {
      return emptyList();
    }

    final List<UnitConversion> convertedUnits = new ArrayList<>();

    final Matcher matcher = PATTERN_CALORIES.matcher(text);
    while (matcher.find()) {
      try {
        final String cals = matcher.group(1).replaceAll(",", "");
        final double calsDecimal = Double.parseDouble(cals);
        final double kj = calsDecimal * KILOJOULE_PER_CALORIE;

        final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
            .inputAmount(NUMBER_FORMAT_CALS.format(calsDecimal))
            .inputUnit("cal")
            .metricAmount(NUMBER_FORMAT_CALS.format(kj))
            .metricUnit("kJ")
            .build();

        convertedUnits.add(conversion);
      } catch (final NumberFormatException | ArithmeticException nfe) {
        LOG.error("Unable to convert text[{}].", text);
      }

    }

    return unmodifiableList(convertedUnits);
  }
}
