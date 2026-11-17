package com.jarsolutions.finances.transaction.application.domain.model;

/**
 * Where a transaction file is in the files directory: every user has a directory named after the
 * username, with the user's files in it.
 */
public record TransactionFileLocation(String username, TransactionFileName name) {

  /** For example {@code juan.albarran/2026.october.transactions.xlsx}. */
  public String value() {
    return "%s/%s".formatted(username, name.value());
  }
}
