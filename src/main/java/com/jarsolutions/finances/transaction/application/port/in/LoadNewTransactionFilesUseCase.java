package com.jarsolutions.finances.transaction.application.port.in;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import java.util.List;

@FunctionalInterface
public interface LoadNewTransactionFilesUseCase {

  /** Loads every file of the files directory, of any user, that was not loaded before. */
  List<TransactionFile> loadNewTransactionFiles();
}
