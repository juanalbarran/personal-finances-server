package com.jarsolutions.finances.user.application.port.in;

import com.jarsolutions.finances.user.application.domain.model.NewUser;
import com.jarsolutions.finances.user.application.domain.model.User;

@FunctionalInterface
public interface CreateUserUseCase {

  /**
   * @throws com.jarsolutions.finances.user.application.domain.model.UserAlreadyExistsException
   *     when another user has the username or the email
   */
  User createUser(NewUser newUser);
}
