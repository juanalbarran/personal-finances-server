package com.jarsolutions.finances.user.application.port.in;

import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.domain.model.UserProfile;

@FunctionalInterface
public interface UpdateUserUseCase {

  /** Changes the email and the names of the user. The username never changes. */
  User updateUser(String username, UserProfile profile);
}
