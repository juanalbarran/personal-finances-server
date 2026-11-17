package com.jarsolutions.finances.category.application.port.in;

import com.jarsolutions.finances.category.application.domain.model.Category;
import java.util.List;

@FunctionalInterface
public interface GetCategoriesUseCase {

  /** The categories of the user, the oldest first. */
  List<Category> getCategories(String username);
}
