package com.jarsolutions.finances.category.application.domain.exception;

/** The user has no category with the id. */
public class CategoryNotFoundException extends RuntimeException {

  public CategoryNotFoundException(long id) {
    super("The category %d does not exist".formatted(id));
  }
}
