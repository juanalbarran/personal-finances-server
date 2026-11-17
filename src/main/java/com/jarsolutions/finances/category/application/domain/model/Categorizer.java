package com.jarsolutions.finances.category.application.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chooses the category of a spend with the rules of a user. The first rule that matches wins: the
 * rules go in the order of {@link RuleField}, the longest pattern first and then the oldest rule.
 */
public record Categorizer(List<CategoryRule> rules) {

  private static final Comparator<CategoryRule> PRECEDENCE =
      Comparator.comparing(CategoryRule::field)
          .thenComparing(rule -> rule.pattern().length(), Comparator.reverseOrder())
          .thenComparing(CategoryRule::id);

  public Categorizer {
    rules = rules.stream().sorted(PRECEDENCE).toList();
  }

  /** Empty when no rule matches the spend. */
  public Optional<Long> categoryOf(Spend spend) {
    return rules.stream()
        .filter(rule -> rule.matches(spend))
        .findFirst()
        .map(CategoryRule::categoryId);
  }
}
