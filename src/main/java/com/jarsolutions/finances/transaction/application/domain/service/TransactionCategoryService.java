package com.jarsolutions.finances.transaction.application.domain.service;

import com.jarsolutions.finances.category.application.port.in.GetCategorizerUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategoryUseCase;
import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.transaction.application.domain.exception.TransactionNotFoundException;
import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import com.jarsolutions.finances.transaction.application.port.in.CategorizeTransactionsUseCase;
import com.jarsolutions.finances.transaction.application.port.in.GetTransactionsUseCase;
import com.jarsolutions.finances.transaction.application.port.in.SetTransactionCategoryUseCase;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionsPort;
import com.jarsolutions.finances.transaction.application.port.out.LoadUncategorizedSpendsPort;
import com.jarsolutions.finances.transaction.application.port.out.UpdateTransactionCategoriesPort;
import com.jarsolutions.finances.transaction.application.port.out.UpdateTransactionCategoryPort;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/** The transactions of a user and the categories they are in. */
@UseCase
@Transactional
class TransactionCategoryService
    implements GetTransactionsUseCase, SetTransactionCategoryUseCase, CategorizeTransactionsUseCase {

  private final GetUserUseCase getUserUseCase;
  private final GetCategoryUseCase getCategoryUseCase;
  private final GetCategorizerUseCase getCategorizerUseCase;
  private final LoadTransactionsPort loadTransactionsPort;
  private final LoadUncategorizedSpendsPort loadUncategorizedSpendsPort;
  private final UpdateTransactionCategoryPort updateTransactionCategoryPort;
  private final UpdateTransactionCategoriesPort updateTransactionCategoriesPort;

  TransactionCategoryService(
      GetUserUseCase getUserUseCase,
      GetCategoryUseCase getCategoryUseCase,
      GetCategorizerUseCase getCategorizerUseCase,
      LoadTransactionsPort loadTransactionsPort,
      LoadUncategorizedSpendsPort loadUncategorizedSpendsPort,
      UpdateTransactionCategoryPort updateTransactionCategoryPort,
      UpdateTransactionCategoriesPort updateTransactionCategoriesPort) {
    this.getUserUseCase = getUserUseCase;
    this.getCategoryUseCase = getCategoryUseCase;
    this.getCategorizerUseCase = getCategorizerUseCase;
    this.loadTransactionsPort = loadTransactionsPort;
    this.loadUncategorizedSpendsPort = loadUncategorizedSpendsPort;
    this.updateTransactionCategoryPort = updateTransactionCategoryPort;
    this.updateTransactionCategoriesPort = updateTransactionCategoriesPort;
  }

  @Override
  @Transactional(readOnly = true)
  public List<StoredTransaction> getTransactions(String username, Optional<YearMonth> period) {
    return loadTransactionsPort.loadTransactions(userIdOf(username), period);
  }

  @Override
  public StoredTransaction setTransactionCategory(
      String username, long transactionId, Optional<Long> categoryId) {
    long userId = userIdOf(username);
    // The category has to be one of the user
    categoryId.ifPresent(id -> getCategoryUseCase.getCategory(userId, id));
    return updateTransactionCategoryPort
        .updateTransactionCategory(userId, transactionId, categoryId)
        .orElseThrow(() -> new TransactionNotFoundException(transactionId));
  }

  @Override
  public int categorizeTransactions(String username) {
    long userId = userIdOf(username);
    Function<Transaction, Optional<Long>> categoryOf =
        TransactionCategorizer.with(getCategorizerUseCase.getCategorizer(userId));
    Map<Long, Long> categoryIdsByTransactionId =
        loadUncategorizedSpendsPort.loadUncategorizedSpends(userId).stream()
            .flatMap(
                stored ->
                    categoryOf
                        .apply(stored.transaction())
                        .map(categoryId -> Map.entry(stored.id(), categoryId))
                        .stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    updateTransactionCategoriesPort.updateTransactionCategories(categoryIdsByTransactionId);
    return categoryIdsByTransactionId.size();
  }

  private long userIdOf(String username) {
    return getUserUseCase.getUser(username).id();
  }
}
