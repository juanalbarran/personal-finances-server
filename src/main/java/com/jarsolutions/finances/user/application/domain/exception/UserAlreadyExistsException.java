package com.jarsolutions.finances.user.application.domain.exception;

/** Another user has the username or the email. */
public class UserAlreadyExistsException extends RuntimeException {

  public UserAlreadyExistsException(String field, String value) {
    super("A user with the %s %s already exists".formatted(field, value));
  }
}
