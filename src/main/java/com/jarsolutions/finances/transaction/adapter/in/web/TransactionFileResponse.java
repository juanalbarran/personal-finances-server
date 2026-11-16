package com.jarsolutions.finances.transaction.adapter.in.web;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import java.time.Instant;

record TransactionFileResponse(
    String fileName, int year, int month, String checksum, Instant loadedAt, int transactionCount) {

  static TransactionFileResponse from(TransactionFile file) {
    return new TransactionFileResponse(
        file.name().value(),
        file.name().period().getYear(),
        file.name().period().getMonthValue(),
        file.checksum(),
        file.loadedAt(),
        file.transactionCount());
  }
}
