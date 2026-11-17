package com.jarsolutions.finances.category.application.domain.model;

/** The data given for a category or a rule is not valid. */
public class InvalidCategoryException extends RuntimeException {

  public InvalidCategoryException(String message) {
    super(message);
  }
}
