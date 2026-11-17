package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import java.util.List;

@FunctionalInterface
public interface LoadUncategorizedSpendsPort {

  /** The transactions of the user that took money out of the account and have no category. */
  List<StoredTransaction> loadUncategorizedSpends(long userId);
}
