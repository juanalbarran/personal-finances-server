package com.jarsolutions.finances.category.application.port.out;

import com.jarsolutions.finances.category.application.domain.model.CategoryRule;

@FunctionalInterface
public interface DeleteCategoryRulePort {

  void deleteCategoryRule(CategoryRule categoryRule);
}
