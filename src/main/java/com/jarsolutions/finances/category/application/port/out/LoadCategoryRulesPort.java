package com.jarsolutions.finances.category.application.port.out;

import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import java.util.List;

@FunctionalInterface
public interface LoadCategoryRulesPort {

  /** The rules of the user, the oldest first. */
  List<CategoryRule> loadCategoryRules(long userId);
}
