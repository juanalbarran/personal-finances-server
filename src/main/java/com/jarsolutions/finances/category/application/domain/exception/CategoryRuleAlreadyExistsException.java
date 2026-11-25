package com.jarsolutions.finances.category.application.domain.exception;

import com.jarsolutions.finances.category.application.domain.model.RuleField;

/** The user already has a rule for the field and the pattern. */
public class CategoryRuleAlreadyExistsException extends RuntimeException {

  public CategoryRuleAlreadyExistsException(RuleField field, String pattern) {
    super("A rule for %s '%s' already exists".formatted(field, pattern));
  }
}
