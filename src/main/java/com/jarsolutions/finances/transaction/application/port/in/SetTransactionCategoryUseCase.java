package com.jarsolutions.finances.transaction.application.port.in;

import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import java.util.Optional;

@FunctionalInterface
public interface SetTransactionCategoryUseCase {

  /** Puts the transaction in a category of the user, or in none when the category is empty. */
  StoredTransaction setTransactionCategory(
      String username, long transactionId, Optional<Long> categoryId);
}
