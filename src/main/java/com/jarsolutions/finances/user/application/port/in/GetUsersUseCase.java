package com.jarsolutions.finances.user.application.port.in;

import com.jarsolutions.finances.user.application.domain.model.User;
import java.util.List;

@FunctionalInterface
public interface GetUsersUseCase {

  /** Every user, ordered by username. */
  List<User> getUsers();
}
