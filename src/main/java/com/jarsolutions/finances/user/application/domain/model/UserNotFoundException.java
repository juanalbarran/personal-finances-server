package com.jarsolutions.finances.user.application.domain.model;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String username) {
    super("The user %s does not exist".formatted(username));
  }
}
