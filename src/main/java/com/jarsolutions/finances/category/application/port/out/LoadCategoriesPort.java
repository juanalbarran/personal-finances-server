package com.jarsolutions.finances.category.application.port.out;

import com.jarsolutions.finances.category.application.domain.model.Category;
import java.util.List;

@FunctionalInterface
public interface LoadCategoriesPort {

  /** The categories of the user, the oldest first. */
  List<Category> loadCategories(long userId);
}
