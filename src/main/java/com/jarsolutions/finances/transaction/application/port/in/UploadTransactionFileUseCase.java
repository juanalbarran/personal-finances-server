package com.jarsolutions.finances.transaction.application.port.in;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;

@FunctionalInterface
public interface UploadTransactionFileUseCase {

  /** Loads the transactions of the file and keeps the file in the files directory. */
  TransactionFile uploadTransactionFile(byte[] content);
}
