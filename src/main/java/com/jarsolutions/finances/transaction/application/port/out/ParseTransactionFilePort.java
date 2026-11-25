package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.Statement;

@FunctionalInterface
public interface ParseTransactionFilePort {

  /**
   * @throws
   *     com.jarsolutions.finances.transaction.application.domain.exception.InvalidTransactionFileException
   *     when the content is not in the format of the bank
   */
  Statement parse(byte[] content);
}
