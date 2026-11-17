package com.jarsolutions.finances.user.application.port.out;

import com.jarsolutions.finances.user.application.domain.model.User;

@FunctionalInterface
public interface DeleteUserPort {

  /**
   * @throws com.jarsolutions.finances.user.application.domain.model.UserHasTransactionsException
   *     when the user has transactions
   */
  void deleteUser(User user);
}
