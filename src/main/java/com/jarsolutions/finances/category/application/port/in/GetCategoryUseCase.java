package com.jarsolutions.finances.category.application.port.in;

import com.jarsolutions.finances.category.application.domain.model.Category;

@FunctionalInterface
public interface GetCategoryUseCase {

  /**
   * @throws com.jarsolutions.finances.category.application.domain.exception.CategoryNotFoundException
   *     when the user has no category with the id
   */
  Category getCategory(long userId, long categoryId);
}
