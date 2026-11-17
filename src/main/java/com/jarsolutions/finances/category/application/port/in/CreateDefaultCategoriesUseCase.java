package com.jarsolutions.finances.category.application.port.in;

@FunctionalInterface
public interface CreateDefaultCategoriesUseCase {

  /** Gives a new user the categories and the rules everybody starts with. */
  void createDefaultCategories(long userId);
}
