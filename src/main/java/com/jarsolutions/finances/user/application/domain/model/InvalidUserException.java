package com.jarsolutions.finances.user.application.domain.model;

/** The data given for a user is not valid. */
public class InvalidUserException extends RuntimeException {

  public InvalidUserException(String message) {
    super(message);
  }
}
