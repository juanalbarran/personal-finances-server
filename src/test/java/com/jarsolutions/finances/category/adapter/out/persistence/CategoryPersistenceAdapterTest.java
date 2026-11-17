package com.jarsolutions.finances.category.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.jarsolutions.finances.category.application.domain.model.Category;
import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.DefaultCategories;
import com.jarsolutions.finances.category.application.domain.model.RuleField;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@Import({CategoryPersistenceAdapter.class, CategoryMapper.class})
class CategoryPersistenceAdapterTest {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  @Autowired private CategoryPersistenceAdapter adapter;
  @Autowired private EntityManager entityManager;

  // The user the changelog inserts
  private long user;

  @BeforeEach
  void user() {
    user =
        ((Number)
                entityManager
                    .createNativeQuery("select id from user_account where username = 'test.user'")
                    .getSingleResult())
            .longValue();
  }

  /** The changelog and {@link DefaultCategories} have to say the same. */
  @Test
  void theUserOfTheChangelogHasTheDefaultCategoriesAndRules() {
    List<Category> categories = adapter.loadCategories(user);
    Map<Long, String> names =
        categories.stream().collect(Collectors.toMap(Category::id, Category::name));

    assertThat(categories).extracting(Category::name).isEqualTo(DefaultCategories.NAMES);
    assertThat(adapter.loadCategoryRules(user))
        .extracting(
            rule ->
                new DefaultCategories.Rule(
                    names.get(rule.categoryId()), rule.field(), rule.pattern()))
        .isEqualTo(DefaultCategories.RULES);
  }

  @Test
  void aCategoryIsCreatedRenamedAndDeletedWithItsRules() {
    Category gifts = adapter.createCategory(user, "Gifts");
    CategoryRule rule =
        adapter.createCategoryRule(user, gifts.id(), RuleField.COUNTERPARTY_NAME, "toy shop");
    assertThat(adapter.loadCategory(user, gifts.id())).contains(gifts);
    assertThat(adapter.loadCategory(user + 1, gifts.id())).isEmpty();
    assertThat(adapter.loadCategoryRules(user)).contains(rule);

    assertThat(adapter.updateCategory(gifts.withName("Presents")).name()).isEqualTo("Presents");
    assertThat(adapter.loadCategories(user)).extracting(Category::name).contains("Presents");

    adapter.deleteCategory(gifts);

    assertThat(adapter.loadCategory(user, gifts.id())).isEmpty();
    assertThat(adapter.loadCategoryRules(user)).doesNotContain(rule);
  }

  @Test
  void aRuleIsDeleted() {
    Category groceries = adapter.loadCategories(user).get(1);
    CategoryRule rule =
        adapter.createCategoryRule(user, groceries.id(), RuleField.COUNTERPARTY_NAME, "market");

    adapter.deleteCategoryRule(rule);
    entityManager.flush();

    assertThat(adapter.loadCategoryRules(user)).doesNotContain(rule);
  }
}
