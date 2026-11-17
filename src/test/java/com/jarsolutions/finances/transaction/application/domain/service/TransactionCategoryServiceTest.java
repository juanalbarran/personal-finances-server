package com.jarsolutions.finances.transaction.application.domain.service;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jarsolutions.finances.category.application.domain.model.Categorizer;
import com.jarsolutions.finances.category.application.domain.model.Category;
import com.jarsolutions.finances.category.application.domain.model.CategoryNotFoundException;
import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.RuleField;
import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionNotFoundException;
import com.jarsolutions.finances.user.application.domain.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TransactionCategoryServiceTest {

  private static final long USER = 7;
  private static final long GROCERIES = 20;
  private static final StoredTransaction SHOP =
      new StoredTransaction(1, purchase("shop", "2026-10-01T10:00:00", "-100"), Optional.empty());

  private final Map<Long, Long> updated = new HashMap<>();

  private final TransactionCategoryService service =
      new TransactionCategoryService(
          username -> new User(USER, username, "juan@example.com", "Juan", "J"),
          (userId, categoryId) ->
              Optional.of(new Category(categoryId, userId, "Groceries"))
                  .filter(category -> category.id() == GROCERIES)
                  .orElseThrow(() -> new CategoryNotFoundException(categoryId)),
          userId ->
              new Categorizer(
                  List.of(
                      new CategoryRule(1, userId, GROCERIES, RuleField.COUNTERPARTY_NAME, "corner"))),
          (userId, period) -> List.of(SHOP),
          userId ->
              List.of(
                  SHOP,
                  new StoredTransaction(
                      2, purchase("other", "KIOSK", "2026-10-02T10:00:00", "-5"), Optional.empty())),
          (userId, transactionId, categoryId) ->
              Optional.of(SHOP)
                  .filter(stored -> stored.id() == transactionId)
                  .map(stored -> new StoredTransaction(stored.id(), stored.transaction(), categoryId)),
          updated::putAll);

  @Test
  void categorize_putsTheSpendsARuleMatchesInItsCategoryAndLeavesTheOthers() {
    assertThat(service.categorizeTransactions("juan")).isEqualTo(1);
    assertThat(updated).containsExactly(Map.entry(1L, GROCERIES));
  }

  @Test
  void setCategory_putsTheTransactionInACategoryOfTheUserOrInNone() {
    assertThat(service.setTransactionCategory("juan", 1, Optional.of(GROCERIES)).categoryId())
        .contains(GROCERIES);
    assertThat(service.setTransactionCategory("juan", 1, Optional.empty()).categoryId()).isEmpty();
  }

  @Test
  void setCategory_whenTheCategoryIsNotOfTheUser_refuses() {
    assertThatThrownBy(() -> service.setTransactionCategory("juan", 1, Optional.of(99L)))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void setCategory_whenTheTransactionIsNotOfTheUser_refuses() {
    assertThatThrownBy(() -> service.setTransactionCategory("juan", 5, Optional.empty()))
        .isInstanceOf(TransactionNotFoundException.class);
  }
}
