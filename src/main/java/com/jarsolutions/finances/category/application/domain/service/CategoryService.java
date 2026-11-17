package com.jarsolutions.finances.category.application.domain.service;

import com.jarsolutions.finances.category.application.domain.model.Category;
import com.jarsolutions.finances.category.application.domain.model.CategoryAlreadyExistsException;
import com.jarsolutions.finances.category.application.domain.model.CategoryNotFoundException;
import com.jarsolutions.finances.category.application.domain.model.DefaultCategories;
import com.jarsolutions.finances.category.application.port.in.CreateCategoryUseCase;
import com.jarsolutions.finances.category.application.port.in.CreateDefaultCategoriesUseCase;
import com.jarsolutions.finances.category.application.port.in.DeleteCategoryUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategoriesUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategoryUseCase;
import com.jarsolutions.finances.category.application.port.in.RenameCategoryUseCase;
import com.jarsolutions.finances.category.application.port.out.CreateCategoryPort;
import com.jarsolutions.finances.category.application.port.out.CreateCategoryRulePort;
import com.jarsolutions.finances.category.application.port.out.DeleteCategoryPort;
import com.jarsolutions.finances.category.application.port.out.LoadCategoriesPort;
import com.jarsolutions.finances.category.application.port.out.LoadCategoryPort;
import com.jarsolutions.finances.category.application.port.out.UpdateCategoryPort;
import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
class CategoryService
    implements GetCategoriesUseCase,
        GetCategoryUseCase,
        CreateCategoryUseCase,
        RenameCategoryUseCase,
        DeleteCategoryUseCase,
        CreateDefaultCategoriesUseCase {

  private final GetUserUseCase getUserUseCase;
  private final LoadCategoriesPort loadCategoriesPort;
  private final LoadCategoryPort loadCategoryPort;
  private final CreateCategoryPort createCategoryPort;
  private final UpdateCategoryPort updateCategoryPort;
  private final DeleteCategoryPort deleteCategoryPort;
  private final CreateCategoryRulePort createCategoryRulePort;

  CategoryService(
      GetUserUseCase getUserUseCase,
      LoadCategoriesPort loadCategoriesPort,
      LoadCategoryPort loadCategoryPort,
      CreateCategoryPort createCategoryPort,
      UpdateCategoryPort updateCategoryPort,
      DeleteCategoryPort deleteCategoryPort,
      CreateCategoryRulePort createCategoryRulePort) {
    this.getUserUseCase = getUserUseCase;
    this.loadCategoriesPort = loadCategoriesPort;
    this.loadCategoryPort = loadCategoryPort;
    this.createCategoryPort = createCategoryPort;
    this.updateCategoryPort = updateCategoryPort;
    this.deleteCategoryPort = deleteCategoryPort;
    this.createCategoryRulePort = createCategoryRulePort;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Category> getCategories(String username) {
    return loadCategoriesPort.loadCategories(userIdOf(username));
  }

  @Override
  @Transactional(readOnly = true)
  public Category getCategory(long userId, long categoryId) {
    return loadCategoryPort
        .loadCategory(userId, categoryId)
        .orElseThrow(() -> new CategoryNotFoundException(categoryId));
  }

  @Override
  public Category createCategory(String username, String name) {
    long userId = userIdOf(username);
    String validName = Category.validName(name);
    refuseTheNameOfAnotherCategory(userId, validName, other -> true);
    return createCategoryPort.createCategory(userId, validName);
  }

  @Override
  public Category renameCategory(String username, long categoryId, String name) {
    Category renamed = getCategory(userIdOf(username), categoryId).withName(name);
    refuseTheNameOfAnotherCategory(
        renamed.userId(), renamed.name(), other -> other.id() != renamed.id());
    return updateCategoryPort.updateCategory(renamed);
  }

  @Override
  public void deleteCategory(String username, long categoryId) {
    deleteCategoryPort.deleteCategory(getCategory(userIdOf(username), categoryId));
  }

  @Override
  public void createDefaultCategories(long userId) {
    Map<String, Category> categories =
        DefaultCategories.NAMES.stream()
            .map(name -> createCategoryPort.createCategory(userId, name))
            .collect(Collectors.toMap(Category::name, Function.identity()));
    DefaultCategories.RULES.forEach(
        rule ->
            createCategoryRulePort.createCategoryRule(
                userId, categories.get(rule.categoryName()).id(), rule.field(), rule.pattern()));
  }

  private long userIdOf(String username) {
    return getUserUseCase.getUser(username).id();
  }

  private void refuseTheNameOfAnotherCategory(
      long userId, String name, Predicate<Category> isAnotherCategory) {
    loadCategoriesPort.loadCategories(userId).stream()
        .filter(isAnotherCategory)
        .filter(other -> other.hasName(name))
        .findFirst()
        .ifPresent(
            other -> {
              throw new CategoryAlreadyExistsException(other.name());
            });
  }
}
