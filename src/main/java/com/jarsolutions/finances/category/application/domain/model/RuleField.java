package com.jarsolutions.finances.category.application.domain.model;

import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * What a rule looks at, never minding the case. They are in order of precedence: a rule about the
 * counterparty wins over one about the type, which wins over one about the bank category.
 */
public enum RuleField {
  /** The name of the merchant or of the other side of a transfer contains the pattern. */
  COUNTERPARTY_NAME(
      Spend::counterpartyName, (value, pattern) -> lowerCase(value).contains(lowerCase(pattern))),
  /** The kind of movement, as the bank names it, is the pattern. */
  TYPE(Spend::type, String::equalsIgnoreCase),
  /** The category the bank assigned is the pattern. */
  BANK_CATEGORY(Spend::bankCategory, String::equalsIgnoreCase);

  private final Function<Spend, String> value;
  private final BiPredicate<String, String> matcher;

  RuleField(Function<Spend, String> value, BiPredicate<String, String> matcher) {
    this.value = value;
    this.matcher = matcher;
  }

  boolean matches(Spend spend, String pattern) {
    return matcher.test(value.apply(spend), pattern);
  }

  private static String lowerCase(String text) {
    return text.toLowerCase(Locale.ROOT);
  }
}
