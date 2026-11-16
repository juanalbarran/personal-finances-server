package com.jarsolutions.finances.transaction.application.domain.model;

import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The name of a transaction file, {@code <year>.<month>.transactions.xlsx}, for example {@code
 * 2026.october.transactions.xlsx}. The period is the month the transactions of the file belong to.
 */
public record TransactionFileName(YearMonth period) {

  private static final Pattern PATTERN =
      Pattern.compile("(\\d{4})\\.([a-z]+)\\.transactions\\.xlsx");

  /** Empty when the text does not follow the naming pattern. */
  public static Optional<TransactionFileName> parse(String fileName) {
    return Optional.of(PATTERN.matcher(fileName))
        .filter(Matcher::matches)
        .flatMap(
            matcher ->
                monthOf(matcher.group(2))
                    .map(month -> YearMonth.of(Integer.parseInt(matcher.group(1)), month)))
        .map(TransactionFileName::new);
  }

  public String value() {
    return "%d.%s.transactions.xlsx".formatted(period.getYear(), nameOf(period.getMonth()));
  }

  private static Optional<Month> monthOf(String name) {
    return Stream.of(Month.values()).filter(month -> nameOf(month).equals(name)).findFirst();
  }

  private static String nameOf(Month month) {
    return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase(Locale.ENGLISH);
  }
}
