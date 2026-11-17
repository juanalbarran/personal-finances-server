package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface LoadTransactionsPort {

  /** The transactions of the user, the oldest first. Of every month when no month is given. */
  List<StoredTransaction> loadTransactions(long userId, Optional<YearMonth> period);
}
