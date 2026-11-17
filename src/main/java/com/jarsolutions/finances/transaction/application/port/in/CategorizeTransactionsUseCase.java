package com.jarsolutions.finances.transaction.application.port.in;

@FunctionalInterface
public interface CategorizeTransactionsUseCase {

  /**
   * Applies the rules of the user to the spends that have no category.
   *
   * @return how many transactions got a category
   */
  int categorizeTransactions(String username);
}
