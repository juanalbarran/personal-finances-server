package com.jarsolutions.finances.transaction.application.port.in;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;

@FunctionalInterface
public interface LoadStoredTransactionFileUseCase {

  /** Loads the transactions of a file that is already in the files directory. */
  TransactionFile loadStoredTransactionFile(TransactionFileName name);
}
