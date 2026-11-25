package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;

@FunctionalInterface
public interface StoreTransactionFilePort {

  /**
   * Writes the file into the directory of its user. Never overwrites a file with other content.
   *
   * @throws
   *     com.jarsolutions.finances.transaction.application.domain.exception.TransactionFileConflictException
   *     when a file with that name and other content is there
   */
  void storeTransactionFile(TransactionFileLocation location, byte[] content);
}
