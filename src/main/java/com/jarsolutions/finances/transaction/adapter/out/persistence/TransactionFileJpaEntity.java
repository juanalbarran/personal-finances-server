package com.jarsolutions.finances.transaction.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "transaction_file")
class TransactionFileJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_account_id", nullable = false)
  private Long userId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "period_year", nullable = false)
  private int periodYear;

  @Column(name = "period_month", nullable = false)
  private int periodMonth;

  @Column(nullable = false, length = 64)
  private String checksum;

  @Column(name = "loaded_at", nullable = false)
  private Instant loadedAt;

  @Column(name = "transaction_count", nullable = false)
  private int transactionCount;

  protected TransactionFileJpaEntity() {}

  TransactionFileJpaEntity(
      Long userId,
      String fileName,
      int periodYear,
      int periodMonth,
      String checksum,
      Instant loadedAt,
      int transactionCount) {
    this.userId = userId;
    this.fileName = fileName;
    this.periodYear = periodYear;
    this.periodMonth = periodMonth;
    this.checksum = checksum;
    this.loadedAt = loadedAt;
    this.transactionCount = transactionCount;
  }

  int getPeriodYear() {
    return periodYear;
  }

  int getPeriodMonth() {
    return periodMonth;
  }

  String getChecksum() {
    return checksum;
  }

  Instant getLoadedAt() {
    return loadedAt;
  }

  int getTransactionCount() {
    return transactionCount;
  }
}
