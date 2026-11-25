package com.jarsolutions.finances.user.application.domain.model;

import com.jarsolutions.finances.user.application.domain.exception.InvalidUserException;

/**
 * A user to create.
 *
 * @param username letters, digits, dots, hyphens, underscores and at signs, 50 at most. It is also
 *     the name of the directory of the user's transaction files
 */
public record NewUser(String username, UserProfile profile) {

  public NewUser {
    username = UserProfile.required("username", username);
    if (!username.matches("[A-Za-z0-9][A-Za-z0-9._@-]{0,49}")) {
      throw new InvalidUserException(
          "The username takes letters, digits, '.', '-', '_' and '@', 50 characters at most");
    }
  }
}
