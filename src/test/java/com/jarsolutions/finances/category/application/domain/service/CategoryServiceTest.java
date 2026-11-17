package com.jarsolutions.finances.category.application.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jarsolutions.finances.category.application.domain.model.Category;
import com.jarsolutions.finances.category.application.domain.model.CategoryAlreadyExistsException;
import com.jarsolutions.finances.category.application.domain.model.CategoryNotFoundException;
import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.CategoryRuleAlreadyExistsException;
import com.jarsolutions.finances.category.application.domain.model.CategoryRuleNotFoundException;
import com.jarsolutions.finances.category.application.domain.model.DefaultCategories;
import com.jarsolutions.finances.category.application.domain.model.InvalidCategoryException;
import com.jarsolutions.finances.category.application.domain.model.RuleField;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CategoryServiceTest {

  private static final long USER = 7;
  private static final GetUserUseCase GET_USER =
      username -> new User(USER, username, "juan@example.com", "Juan", "J");

  // The database, in memory
  private final AtomicLong ids = new AtomicLong();
  private final List<Category> categories = new ArrayList<>();
  private final List<CategoryRule> rules = new ArrayList<>();

  private final CategoryService categoryService =
      new CategoryService(
          GET_USER,
          userId -> categories.stream().filter(category -> category.userId() == userId).toList(),
          (userId, id) ->
              categories.stream()
                  .filter(category -> category.userId() == userId && category.id() == id)
                  .findFirst(),
          (userId, name) -> add(categories, new Category(ids.incrementAndGet(), userId, name)),
          category -> {
            categories.replaceAll(old -> old.id() == category.id() ? category : old);
            return category;
          },
          categories::remove,
          this::createRule);

  private final CategoryRuleService ruleService =
      new CategoryRuleService(
          GET_USER,
          categoryService,
          userId -> rules.stream().filter(rule -> rule.userId() == userId).toList(),
          this::createRule,
          rules::remove);

  @Test
  void createDefaultCategories_givesTheUserTheCategoriesAndTheirRules() {
    categoryService.createDefaultCategories(USER);

    assertThat(categoryService.getCategories("juan"))
        .extracting(Category::name)
        .isEqualTo(DefaultCategories.NAMES);
    assertThat(ruleService.getCategoryRules("juan")).hasSameSizeAs(DefaultCategories.RULES);
    long flights = categories.stream().filter(c -> c.hasName("Flights")).findFirst().get().id();
    assertThat(rules)
        .filteredOn(rule -> rule.categoryId() == flights)
        .extracting(CategoryRule::pattern)
        .containsExactly("Ryanair", "Wizz Air", "klm.com");
  }

  @Test
  void create_stripsTheNameAndRefusesAnEmptyOrRepeatedOne() {
    assertThat(categoryService.createCategory("juan", "  Gifts ").name()).isEqualTo("Gifts");

    assertThatThrownBy(() -> categoryService.createCategory("juan", "gifts"))
        .isInstanceOf(CategoryAlreadyExistsException.class);
    assertThatThrownBy(() -> categoryService.createCategory("juan", "  "))
        .isInstanceOf(InvalidCategoryException.class);
    assertThatThrownBy(() -> categoryService.createCategory("juan", null))
        .isInstanceOf(InvalidCategoryException.class);
  }

  @Test
  void rename_changesTheNameUnlessAnotherCategoryHasIt() {
    Category gifts = categoryService.createCategory("juan", "Gifts");
    categoryService.createCategory("juan", "Pets");

    assertThat(categoryService.renameCategory("juan", gifts.id(), "GIFTS").name())
        .isEqualTo("GIFTS");
    assertThatThrownBy(() -> categoryService.renameCategory("juan", gifts.id(), "pets"))
        .isInstanceOf(CategoryAlreadyExistsException.class);
    assertThatThrownBy(() -> categoryService.renameCategory("juan", 99, "Other"))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void delete_removesTheCategoryOfTheUser() {
    Category gifts = categoryService.createCategory("juan", "Gifts");

    categoryService.deleteCategory("juan", gifts.id());

    assertThat(categoryService.getCategories("juan")).isEmpty();
    assertThatThrownBy(() -> categoryService.deleteCategory("juan", gifts.id()))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void rules_areCreatedForACategoryOfTheUserOncePerFieldAndPattern_andDeleted() {
    Category gifts = categoryService.createCategory("juan", "Gifts");

    CategoryRule rule =
        ruleService.createCategoryRule("juan", gifts.id(), RuleField.COUNTERPARTY_NAME, " toys ");

    assertThat(rule.pattern()).isEqualTo("toys");
    assertThat(ruleService.getCategorizer(USER).rules()).containsExactly(rule);
    assertThatThrownBy(
            () ->
                ruleService.createCategoryRule(
                    "juan", gifts.id(), RuleField.COUNTERPARTY_NAME, "TOYS"))
        .isInstanceOf(CategoryRuleAlreadyExistsException.class);
    assertThatThrownBy(
            () -> ruleService.createCategoryRule("juan", 99, RuleField.TYPE, "something"))
        .isInstanceOf(CategoryNotFoundException.class);
    assertThatThrownBy(() -> ruleService.createCategoryRule("juan", gifts.id(), null, "x"))
        .isInstanceOf(InvalidCategoryException.class);
    assertThatThrownBy(() -> ruleService.createCategoryRule("juan", gifts.id(), RuleField.TYPE, ""))
        .isInstanceOf(InvalidCategoryException.class);

    ruleService.deleteCategoryRule("juan", rule.id());

    assertThat(ruleService.getCategoryRules("juan")).isEmpty();
    assertThatThrownBy(() -> ruleService.deleteCategoryRule("juan", rule.id()))
        .isInstanceOf(CategoryRuleNotFoundException.class);
  }

  private CategoryRule createRule(long userId, long categoryId, RuleField field, String pattern) {
    return add(rules, new CategoryRule(ids.incrementAndGet(), userId, categoryId, field, pattern));
  }

  private static <T> T add(List<T> list, T element) {
    list.add(element);
    return element;
  }
}
