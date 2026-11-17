package com.jarsolutions.finances.transaction.adapter.in.web;

import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @param categoryId null while the transaction has no category
 */
record TransactionResponse(
    long id,
    Long categoryId,
    String bankReference,
    String accountNumber,
    String counterpartyAccountNumber,
    String counterpartyName,
    String type,
    String memo,
    String bankCategory,
    LocalDateTime transactionTime,
    LocalDate bookingDate,
    BigDecimal amount,
    String currency) {

  static TransactionResponse from(StoredTransaction stored) {
    Transaction transaction = stored.transaction();
    return new TransactionResponse(
        stored.id(),
        stored.categoryId().orElse(null),
        transaction.bankReference(),
        transaction.accountNumber(),
        transaction.counterpartyAccountNumber().orElse(null),
        transaction.counterpartyName(),
        transaction.type(),
        transaction.memo().orElse(null),
        transaction.bankCategory(),
        transaction.transactionTime(),
        transaction.bookingDate(),
        transaction.amount(),
        transaction.currency());
  }
}
