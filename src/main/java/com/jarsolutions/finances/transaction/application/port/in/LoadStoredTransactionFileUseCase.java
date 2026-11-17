package com.jarsolutions.finances.transaction.application.port.in;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;

@FunctionalInterface
public interface LoadStoredTransactionFileUseCase {

  /** Loads the transactions of a file that is already in the directory of its user. */
  TransactionFile loadStoredTransactionFile(TransactionFileLocation location);
}
