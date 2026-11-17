package com.jarsolutions.finances.transaction.application.port.out;

import java.util.Map;

@FunctionalInterface
public interface UpdateTransactionCategoriesPort {

  /** Puts each transaction in its category. */
  void updateTransactionCategories(Map<Long, Long> categoryIdsByTransactionId);
}
