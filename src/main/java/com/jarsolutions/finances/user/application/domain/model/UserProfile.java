package com.jarsolutions.finances.user.application.domain.model;

import com.jarsolutions.finances.user.application.domain.exception.InvalidUserException;
import java.util.Optional;
import java.util.function.Predicate;

/** The data of a user that can be edited. */
public record UserProfile(String email, String name, String lastName) {

  private static final int MAX_LENGTH = 255;

  public UserProfile {
    email = required("email", email);
    name = required("name", name);
    lastName = required("lastName", lastName);
    if (!email.matches("[^@\\s]+@[^@\\s]+")) {
      throw new InvalidUserException("The email is not an email address");
    }
  }

  static String required(String field, String value) {
    return Optional.ofNullable(value)
        .map(String::strip)
        .filter(Predicate.not(String::isEmpty))
        .filter(text -> text.length() <= MAX_LENGTH)
        .orElseThrow(
            () ->
                new InvalidUserException(
                    "The %s is required, %d characters at most".formatted(field, MAX_LENGTH)));
  }
}
