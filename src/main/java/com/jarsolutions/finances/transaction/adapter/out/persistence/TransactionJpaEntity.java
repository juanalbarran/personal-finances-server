package com.jarsolutions.finances.transaction.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_transaction")
class TransactionJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transaction_file_id", nullable = false)
  private TransactionFileJpaEntity transactionFile;

  @Column(name = "bank_reference", nullable = false, unique = true, length = 64)
  private String bankReference;

  @Column(name = "account_number", nullable = false, length = 64)
  private String accountNumber;

  @Column(name = "counterparty_account_number", length = 64)
  private String counterpartyAccountNumber;

  @Column(name = "counterparty_name", nullable = false)
  private String counterpartyName;

  @Column(nullable = false)
  private String type;

  @Column(length = 1000)
  private String memo;

  @Column(name = "bank_category", nullable = false)
  private String bankCategory;

  @Column(name = "transaction_time", nullable = false)
  private LocalDateTime transactionTime;

  @Column(name = "booking_date", nullable = false)
  private LocalDate bookingDate;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  protected TransactionJpaEntity() {}

  TransactionJpaEntity(
      TransactionFileJpaEntity transactionFile,
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
    this.transactionFile = transactionFile;
    this.bankReference = bankReference;
    this.accountNumber = accountNumber;
    this.counterpartyAccountNumber = counterpartyAccountNumber;
    this.counterpartyName = counterpartyName;
    this.type = type;
    this.memo = memo;
    this.bankCategory = bankCategory;
    this.transactionTime = transactionTime;
    this.bookingDate = bookingDate;
    this.amount = amount;
    this.currency = currency;
  }

  String getBankReference() {
    return bankReference;
  }

  String getCounterpartyAccountNumber() {
    return counterpartyAccountNumber;
  }

  String getMemo() {
    return memo;
  }

  BigDecimal getAmount() {
    return amount;
  }
}
