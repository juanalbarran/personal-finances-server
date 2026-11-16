package com.jarsolutions.finances.transaction.adapter.out.persistence;

import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import java.time.YearMonth;
import org.springframework.stereotype.Component;

@Component
class TransactionMapper {

  TransactionFile toDomain(TransactionFileJpaEntity entity) {
    return new TransactionFile(
        new TransactionFileName(YearMonth.of(entity.getPeriodYear(), entity.getPeriodMonth())),
        entity.getChecksum(),
        entity.getLoadedAt(),
        entity.getTransactionCount());
  }

  TransactionFileJpaEntity toEntity(TransactionFile file) {
    return new TransactionFileJpaEntity(
        file.name().value(),
        file.name().period().getYear(),
        file.name().period().getMonthValue(),
        file.checksum(),
        file.loadedAt(),
        file.transactionCount());
  }

  TransactionJpaEntity toEntity(Transaction transaction, TransactionFileJpaEntity file) {
    return new TransactionJpaEntity(
        file,
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
