package com.jarsolutions.finances.category.adapter.out.persistence;

import com.jarsolutions.finances.category.application.domain.exception.CategoryNotFoundException;
import com.jarsolutions.finances.category.application.domain.model.Category;
import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.RuleField;
import com.jarsolutions.finances.category.application.port.out.CreateCategoryPort;
import com.jarsolutions.finances.category.application.port.out.CreateCategoryRulePort;
import com.jarsolutions.finances.category.application.port.out.DeleteCategoryPort;
import com.jarsolutions.finances.category.application.port.out.DeleteCategoryRulePort;
import com.jarsolutions.finances.category.application.port.out.LoadCategoriesPort;
import com.jarsolutions.finances.category.application.port.out.LoadCategoryPort;
import com.jarsolutions.finances.category.application.port.out.LoadCategoryRulesPort;
import com.jarsolutions.finances.category.application.port.out.UpdateCategoryPort;
import com.jarsolutions.finances.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
class CategoryPersistenceAdapter
    implements LoadCategoriesPort,
        LoadCategoryPort,
        CreateCategoryPort,
        UpdateCategoryPort,
        DeleteCategoryPort,
        LoadCategoryRulesPort,
        CreateCategoryRulePort,
        DeleteCategoryRulePort {

  private final CategoryJpaRepository categoryRepository;
  private final CategoryRuleJpaRepository categoryRuleRepository;
  private final CategoryMapper categoryMapper;
  private final EntityManager entityManager;

  CategoryPersistenceAdapter(
      CategoryJpaRepository categoryRepository,
      CategoryRuleJpaRepository categoryRuleRepository,
      CategoryMapper categoryMapper,
      EntityManager entityManager) {
    this.categoryRepository = categoryRepository;
    this.categoryRuleRepository = categoryRuleRepository;
    this.categoryMapper = categoryMapper;
    this.entityManager = entityManager;
  }

  @Override
  public List<Category> loadCategories(long userId) {
    return categoryRepository.findAllByUserIdOrderById(userId).stream()
        .map(categoryMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Category> loadCategory(long userId, long categoryId) {
    return categoryRepository.findByIdAndUserId(categoryId, userId).map(categoryMapper::toDomain);
  }

  @Override
  public Category createCategory(long userId, String name) {
    return categoryMapper.toDomain(
        categoryRepository.saveAndFlush(new CategoryJpaEntity(userId, name)));
  }

  @Override
  public Category updateCategory(Category category) {
    CategoryJpaEntity entity =
        categoryRepository
            .findByIdAndUserId(category.id(), category.userId())
            .orElseThrow(() -> new CategoryNotFoundException(category.id()));
    entity.rename(category.name());
    return categoryMapper.toDomain(categoryRepository.saveAndFlush(entity));
  }

  /**
   * The database deletes the rules of the category and leaves its transactions without category,
   * so what Hibernate has in memory is forgotten.
   */
  @Override
  public void deleteCategory(Category category) {
    categoryRepository.deleteById(category.id());
    categoryRepository.flush();
    entityManager.clear();
  }

  @Override
  public List<CategoryRule> loadCategoryRules(long userId) {
    return categoryRuleRepository.findAllByUserIdOrderById(userId).stream()
        .map(categoryMapper::toDomain)
        .toList();
  }

  @Override
  public CategoryRule createCategoryRule(
      long userId, long categoryId, RuleField field, String pattern) {
    return categoryMapper.toDomain(
        categoryRuleRepository.saveAndFlush(
            new CategoryRuleJpaEntity(userId, categoryId, field, pattern)));
  }

  @Override
  public void deleteCategoryRule(CategoryRule categoryRule) {
    categoryRuleRepository.deleteById(categoryRule.id());
  }
}
