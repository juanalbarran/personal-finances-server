package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import java.util.List;

@FunctionalInterface
public interface LoadTransactionFilesPort {

  /** Every file of the user that was loaded into the database. */
  List<TransactionFile> loadTransactionFiles(long userId);
}
