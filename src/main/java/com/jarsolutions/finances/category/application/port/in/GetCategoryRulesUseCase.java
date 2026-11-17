package com.jarsolutions.finances.category.application.port.in;

import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import java.util.List;

@FunctionalInterface
public interface GetCategoryRulesUseCase {

  /** The rules of the user, the oldest first. */
  List<CategoryRule> getCategoryRules(String username);
}
