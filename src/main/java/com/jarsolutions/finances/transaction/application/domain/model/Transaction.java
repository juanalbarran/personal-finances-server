package com.jarsolutions.finances.transaction.application.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

/**
 * One movement of the bank account, as the bank reports it.
 *
 * @param bankReference the identifier the bank gives the movement, unique
 * @param amount negative when money leaves the account
 * @param bankCategory the category the bank assigned, kept as it comes
 */
public record Transaction(
    String bankReference,
    String accountNumber,
    Optional<String> counterpartyAccountNumber,
    String counterpartyName,
    String type,
    Optional<String> memo,
    String bankCategory,
    LocalDateTime transactionTime,
    LocalDate bookingDate,
    BigDecimal amount,
    String currency) {

  /** The month the movement happened in, which can be before the month it was booked in. */
  public YearMonth period() {
    return YearMonth.from(transactionTime);
  }
}
