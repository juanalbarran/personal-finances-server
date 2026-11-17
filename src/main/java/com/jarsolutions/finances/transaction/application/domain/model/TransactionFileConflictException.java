package com.jarsolutions.finances.transaction.application.domain.model;

/** A different file of the user is already there for the same month. */
public class TransactionFileConflictException extends RuntimeException {

  public TransactionFileConflictException(TransactionFileName name) {
    super("A different %s already exists".formatted(name.value()));
  }
}
