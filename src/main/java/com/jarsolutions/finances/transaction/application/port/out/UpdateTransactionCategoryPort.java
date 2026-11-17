package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import java.util.Optional;

@FunctionalInterface
public interface UpdateTransactionCategoryPort {

  /** Empty when the user has no transaction with the id. */
  Optional<StoredTransaction> updateTransactionCategory(
      long userId, long transactionId, Optional<Long> categoryId);
}
