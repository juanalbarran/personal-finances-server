package com.jarsolutions.finances.transaction.application.port.in;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;

@FunctionalInterface
public interface UploadTransactionFileUseCase {

  /** Loads the transactions of the file for the user and keeps the file in the user's directory. */
  TransactionFile uploadTransactionFile(String username, byte[] content);
}
