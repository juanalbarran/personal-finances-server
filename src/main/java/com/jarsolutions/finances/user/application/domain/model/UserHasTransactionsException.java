package com.jarsolutions.finances.user.application.domain.model;

/** A user that has transactions is not deleted. */
public class UserHasTransactionsException extends RuntimeException {

  public UserHasTransactionsException(String username) {
    super("The user %s has transactions".formatted(username));
  }
}
