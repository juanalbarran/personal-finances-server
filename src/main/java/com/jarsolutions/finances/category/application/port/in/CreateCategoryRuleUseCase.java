package com.jarsolutions.finances.category.application.port.in;

import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.RuleField;

@FunctionalInterface
public interface CreateCategoryRuleUseCase {

  /**
   * @throws com.jarsolutions.finances.category.application.domain.exception.CategoryRuleAlreadyExistsException
   *     when the user already has a rule for the field and the pattern
   */
  CategoryRule createCategoryRule(String username, long categoryId, RuleField field, String pattern);
}
