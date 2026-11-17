package com.jarsolutions.finances.category.application.port.in;

import com.jarsolutions.finances.category.application.domain.model.Category;

@FunctionalInterface
public interface CreateCategoryUseCase {

  /**
   * @throws com.jarsolutions.finances.category.application.domain.model.CategoryAlreadyExistsException
   *     when the user already has a category with the name
   */
  Category createCategory(String username, String name);
}
