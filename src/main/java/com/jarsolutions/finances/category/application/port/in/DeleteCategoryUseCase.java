package com.jarsolutions.finances.category.application.port.in;

@FunctionalInterface
public interface DeleteCategoryUseCase {

  /** Deletes the category with its rules. Its transactions are left without category. */
  void deleteCategory(String username, long categoryId);
}
