package com.jarsolutions.finances.user.application.domain.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String username) {
    super("The user %s does not exist".formatted(username));
  }
}
