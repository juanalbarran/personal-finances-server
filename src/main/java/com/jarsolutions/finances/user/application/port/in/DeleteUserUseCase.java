package com.jarsolutions.finances.user.application.port.in;

@FunctionalInterface
public interface DeleteUserUseCase {

  /**
   * Deletes the user with its categories and rules.
   *
   * @throws com.jarsolutions.finances.user.application.domain.model.UserHasTransactionsException
   *     when the user has transactions
   */
  void deleteUser(String username);
}
