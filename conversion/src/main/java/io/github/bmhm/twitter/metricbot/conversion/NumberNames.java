package io.github.bmhm.twitter.metricbot.conversion;

import static java.util.Arrays.asList;


import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class NumberNames {

  public static final String ONE = "one";
  public static final String TWO = "two";
  public static final String THREE = "three";
  public static final String FOUR = "four";
  public static final String FIVE = "five";
  public static final String SIX = "six";
  public static final String SEVEN = "seven";
  public static final String EIGHT = "eight";
  public static final String NINE = "nine";
  public static final String TEN = "ten";
  public static final String ELEVEN = "eleven";
  public static final String TWELVE = "twelve";

  public static final List<String> ALL = asList(
      ONE,
      TWO,
      THREE,
      FOUR,
      FIVE,
      SIX,
      SEVEN,
      EIGHT,
      NINE,
      TEN,
      ELEVEN,
      TWELVE
  );

  private NumberNames() {
    // util.
  }

  public static Optional<Double> convert(final String input) {
    for (int ii = 0; ii < ALL.size(); ii++) {
      if (ALL.get(ii).equals(input.toLowerCase(Locale.ENGLISH))) {
        return Optional.of((ii + 1)* 1.0d);
      }
    }

    return Optional.empty();
  }

}
