package com.jarsolutions.finances.user.application.domain.model;

/**
 * A person who owns finances in this application. The username is the one the authentication
 * service knows the person by, so it never changes.
 */
public record User(long id, String username, String email, String name, String lastName) {

  public User withProfile(UserProfile profile) {
    return new User(id, username, profile.email(), profile.name(), profile.lastName());
  }
}
