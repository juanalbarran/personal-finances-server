package com.jarsolutions.finances.transaction.application.domain.model;

/** The user has no transaction with the id. */
public class TransactionNotFoundException extends RuntimeException {

  public TransactionNotFoundException(long id) {
    super("The transaction %d does not exist".formatted(id));
  }
}
