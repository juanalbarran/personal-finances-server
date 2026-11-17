package com.jarsolutions.finances.category.application.domain.model;

/** The user already has a category with the name. */
public class CategoryAlreadyExistsException extends RuntimeException {

  public CategoryAlreadyExistsException(String name) {
    super("The category %s already exists".formatted(name));
  }
}
