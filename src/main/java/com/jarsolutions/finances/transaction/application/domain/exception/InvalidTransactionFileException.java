package com.jarsolutions.finances.transaction.application.domain.exception;

/** The content is not a transaction file in the format of the bank. */
public class InvalidTransactionFileException extends RuntimeException {

  public InvalidTransactionFileException(String message) {
    super(message);
  }

  public InvalidTransactionFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
