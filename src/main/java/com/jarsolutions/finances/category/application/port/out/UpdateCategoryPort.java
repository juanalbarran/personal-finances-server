package com.jarsolutions.finances.category.application.port.out;

import com.jarsolutions.finances.category.application.domain.model.Category;

@FunctionalInterface
public interface UpdateCategoryPort {

  Category updateCategory(Category category);
}
