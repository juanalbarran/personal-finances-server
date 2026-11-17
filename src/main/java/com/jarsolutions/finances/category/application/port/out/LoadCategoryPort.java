package com.jarsolutions.finances.category.application.port.out;

import com.jarsolutions.finances.category.application.domain.model.Category;
import java.util.Optional;

@FunctionalInterface
public interface LoadCategoryPort {

  Optional<Category> loadCategory(long userId, long categoryId);
}
