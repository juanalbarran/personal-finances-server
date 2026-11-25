package com.jarsolutions.finances.user.application.port.in;

import com.jarsolutions.finances.user.application.domain.model.User;

@FunctionalInterface
public interface GetUserUseCase {

  /**
   * @throws com.jarsolutions.finances.user.application.domain.exception.UserNotFoundException when
   *     nobody has the username
   */
  User getUser(String username);
}
