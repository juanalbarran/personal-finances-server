package com.jarsolutions.finances.category.adapter.in.web;

import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.RuleField;

record CategoryRuleResponse(long id, long categoryId, RuleField field, String pattern) {

  static CategoryRuleResponse from(CategoryRule rule) {
    return new CategoryRuleResponse(rule.id(), rule.categoryId(), rule.field(), rule.pattern());
  }
}
