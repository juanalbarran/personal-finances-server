package com.jarsolutions.finances.category.application.domain.model;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Puts the spends it matches in a category.
 *
 * @param pattern the text the field of the spend contains or equals, see {@link RuleField}
 */
public record CategoryRule(long id, long userId, long categoryId, RuleField field, String pattern) {

  public static final int MAX_PATTERN_LENGTH = 255;

  public boolean matches(Spend spend) {
    return field.matches(spend, pattern);
  }

  public boolean isAbout(RuleField field, String pattern) {
    return this.field == field && this.pattern.equalsIgnoreCase(pattern);
  }

  public static RuleField validField(RuleField field) {
    return Optional.ofNullable(field)
        .orElseThrow(() -> new InvalidCategoryException("The field is required"));
  }

  /** The pattern without the spaces around it. */
  public static String validPattern(String pattern) {
    return Optional.ofNullable(pattern)
        .map(String::strip)
        .filter(Predicate.not(String::isEmpty))
        .filter(text -> text.length() <= MAX_PATTERN_LENGTH)
        .orElseThrow(
            () ->
                new InvalidCategoryException(
                    "The pattern is required, %d characters at most"
                        .formatted(MAX_PATTERN_LENGTH)));
  }
}
