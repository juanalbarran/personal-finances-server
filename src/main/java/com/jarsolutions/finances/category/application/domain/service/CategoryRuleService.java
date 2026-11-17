package com.jarsolutions.finances.category.application.domain.service;

import com.jarsolutions.finances.category.application.domain.model.Categorizer;
import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.CategoryRuleAlreadyExistsException;
import com.jarsolutions.finances.category.application.domain.model.CategoryRuleNotFoundException;
import com.jarsolutions.finances.category.application.domain.model.RuleField;
import com.jarsolutions.finances.category.application.port.in.CreateCategoryRuleUseCase;
import com.jarsolutions.finances.category.application.port.in.DeleteCategoryRuleUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategorizerUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategoryRulesUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategoryUseCase;
import com.jarsolutions.finances.category.application.port.out.CreateCategoryRulePort;
import com.jarsolutions.finances.category.application.port.out.DeleteCategoryRulePort;
import com.jarsolutions.finances.category.application.port.out.LoadCategoryRulesPort;
import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
class CategoryRuleService
    implements GetCategoryRulesUseCase,
        CreateCategoryRuleUseCase,
        DeleteCategoryRuleUseCase,
        GetCategorizerUseCase {

  private final GetUserUseCase getUserUseCase;
  private final GetCategoryUseCase getCategoryUseCase;
  private final LoadCategoryRulesPort loadCategoryRulesPort;
  private final CreateCategoryRulePort createCategoryRulePort;
  private final DeleteCategoryRulePort deleteCategoryRulePort;

  CategoryRuleService(
      GetUserUseCase getUserUseCase,
      GetCategoryUseCase getCategoryUseCase,
      LoadCategoryRulesPort loadCategoryRulesPort,
      CreateCategoryRulePort createCategoryRulePort,
      DeleteCategoryRulePort deleteCategoryRulePort) {
    this.getUserUseCase = getUserUseCase;
    this.getCategoryUseCase = getCategoryUseCase;
    this.loadCategoryRulesPort = loadCategoryRulesPort;
    this.createCategoryRulePort = createCategoryRulePort;
    this.deleteCategoryRulePort = deleteCategoryRulePort;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CategoryRule> getCategoryRules(String username) {
    return loadCategoryRulesPort.loadCategoryRules(userIdOf(username));
  }

  @Override
  public CategoryRule createCategoryRule(
      String username, long categoryId, RuleField field, String pattern) {
    long userId = userIdOf(username);
    RuleField validField = CategoryRule.validField(field);
    String validPattern = CategoryRule.validPattern(pattern);
    // The category has to be one of the user
    getCategoryUseCase.getCategory(userId, categoryId);
    loadCategoryRulesPort.loadCategoryRules(userId).stream()
        .filter(rule -> rule.isAbout(validField, validPattern))
        .findFirst()
        .ifPresent(
            rule -> {
              throw new CategoryRuleAlreadyExistsException(rule.field(), rule.pattern());
            });
    return createCategoryRulePort.createCategoryRule(userId, categoryId, validField, validPattern);
  }

  @Override
  public void deleteCategoryRule(String username, long categoryRuleId) {
    loadCategoryRulesPort.loadCategoryRules(userIdOf(username)).stream()
        .filter(rule -> rule.id() == categoryRuleId)
        .findFirst()
        .ifPresentOrElse(
            deleteCategoryRulePort::deleteCategoryRule,
            () -> {
              throw new CategoryRuleNotFoundException(categoryRuleId);
            });
  }

  @Override
  @Transactional(readOnly = true)
  public Categorizer getCategorizer(long userId) {
    return new Categorizer(loadCategoryRulesPort.loadCategoryRules(userId));
  }

  private long userIdOf(String username) {
    return getUserUseCase.getUser(username).id();
  }
}
