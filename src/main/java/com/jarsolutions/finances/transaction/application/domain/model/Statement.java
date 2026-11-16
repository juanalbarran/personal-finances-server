package com.jarsolutions.finances.transaction.application.domain.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * The content of a transaction file.
 *
 * @param queryEnd the last day of the range the file was exported for
 */
public record Statement(LocalDate queryEnd, List<Transaction> transactions) {

  public Statement {
    transactions = List.copyOf(transactions);
  }

  /** The month the file was exported for. */
  public YearMonth period() {
    return YearMonth.from(queryEnd);
  }

  public List<Transaction> transactionsOf(YearMonth period) {
    return transactions.stream()
        .filter(transaction -> transaction.period().equals(period))
        .toList();
  }
}
