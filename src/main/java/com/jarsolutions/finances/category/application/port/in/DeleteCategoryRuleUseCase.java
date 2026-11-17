package com.jarsolutions.finances.category.application.port.in;

@FunctionalInterface
public interface DeleteCategoryRuleUseCase {

  void deleteCategoryRule(String username, long categoryRuleId);
}
