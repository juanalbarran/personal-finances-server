package com.jarsolutions.finances.category.application.domain.exception;

/** The user has no rule with the id. */
public class CategoryRuleNotFoundException extends RuntimeException {

  public CategoryRuleNotFoundException(long id) {
    super("The category rule %d does not exist".formatted(id));
  }
}
