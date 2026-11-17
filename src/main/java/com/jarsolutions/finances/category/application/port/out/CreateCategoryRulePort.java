package com.jarsolutions.finances.category.application.port.out;

import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.RuleField;

@FunctionalInterface
public interface CreateCategoryRulePort {

  CategoryRule createCategoryRule(long userId, long categoryId, RuleField field, String pattern);
}
