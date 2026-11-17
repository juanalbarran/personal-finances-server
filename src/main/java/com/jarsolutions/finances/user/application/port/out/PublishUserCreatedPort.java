package com.jarsolutions.finances.user.application.port.out;

import com.jarsolutions.finances.user.application.domain.model.UserCreated;

@FunctionalInterface
public interface PublishUserCreatedPort {

  /** The listeners run before this returns, in the same database transaction. */
  void publish(UserCreated userCreated);
}
