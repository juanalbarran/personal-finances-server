package com.jarsolutions.finances.category.application.domain.model;

import com.jarsolutions.finances.category.application.domain.exception.InvalidCategoryException;
import java.util.Optional;
import java.util.function.Predicate;

/** One of the groups a user divides the monthly spends in, like Groceries or Flights. */
public record Category(long id, long userId, String name) {

  public static final int MAX_NAME_LENGTH = 100;

  public Category withName(String name) {
    return new Category(id, userId, validName(name));
  }

  public boolean hasName(String name) {
    return this.name.equalsIgnoreCase(name);
  }

  /** The name without the spaces around it. */
  public static String validName(String name) {
    return Optional.ofNullable(name)
        .map(String::strip)
        .filter(Predicate.not(String::isEmpty))
        .filter(text -> text.length() <= MAX_NAME_LENGTH)
        .orElseThrow(
            () ->
                new InvalidCategoryException(
                    "The name is required, %d characters at most".formatted(MAX_NAME_LENGTH)));
  }
}
