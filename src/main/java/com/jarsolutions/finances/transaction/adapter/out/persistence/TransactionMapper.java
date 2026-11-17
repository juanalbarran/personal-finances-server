package com.jarsolutions.finances.transaction.adapter.out.persistence;

import com.jarsolutions.finances.transaction.application.domain.model.CategorizedTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import java.time.YearMonth;
import java.util.Optional;
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

  StoredTransaction toDomain(TransactionJpaEntity entity) {
    return new StoredTransaction(
        entity.getId(),
        new Transaction(
            entity.getBankReference(),
            entity.getAccountNumber(),
            Optional.ofNullable(entity.getCounterpartyAccountNumber()),
            entity.getCounterpartyName(),
            entity.getType(),
            Optional.ofNullable(entity.getMemo()),
            entity.getBankCategory(),
            entity.getTransactionTime(),
            entity.getBookingDate(),
            entity.getAmount(),
            entity.getCurrency()),
        Optional.ofNullable(entity.getCategoryId()));
  }

  TransactionFileJpaEntity toEntity(long userId, TransactionFile file) {
    return new TransactionFileJpaEntity(
        userId,
        file.name().value(),
        file.name().period().getYear(),
        file.name().period().getMonthValue(),
        file.checksum(),
        file.loadedAt(),
        file.transactionCount());
  }

  TransactionJpaEntity toEntity(
      long userId, CategorizedTransaction categorized, TransactionFileJpaEntity file) {
    Transaction transaction = categorized.transaction();
    return new TransactionJpaEntity(
        file,
        userId,
        categorized.categoryId().orElse(null),
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
