package com.jarsolutions.finances.category.application.port.in;

import com.jarsolutions.finances.category.application.domain.model.Category;

@FunctionalInterface
public interface RenameCategoryUseCase {

  Category renameCategory(String username, long categoryId, String name);
}
