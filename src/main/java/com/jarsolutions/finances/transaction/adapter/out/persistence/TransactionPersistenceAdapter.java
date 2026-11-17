package com.jarsolutions.finances.transaction.adapter.out.persistence;

import com.jarsolutions.finances.common.PersistenceAdapter;
import com.jarsolutions.finances.transaction.application.domain.model.CategorizedTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.port.out.LoadBankReferencesPort;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionFilesPort;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionsPort;
import com.jarsolutions.finances.transaction.application.port.out.LoadUncategorizedSpendsPort;
import com.jarsolutions.finances.transaction.application.port.out.SaveTransactionFilePort;
import com.jarsolutions.finances.transaction.application.port.out.UpdateTransactionCategoriesPort;
import com.jarsolutions.finances.transaction.application.port.out.UpdateTransactionCategoryPort;
import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@PersistenceAdapter
class TransactionPersistenceAdapter
    implements LoadTransactionFilesPort,
        LoadBankReferencesPort,
        SaveTransactionFilePort,
        LoadTransactionsPort,
        LoadUncategorizedSpendsPort,
        UpdateTransactionCategoryPort,
        UpdateTransactionCategoriesPort {

  private final TransactionFileJpaRepository transactionFileRepository;
  private final TransactionJpaRepository transactionRepository;
  private final TransactionMapper transactionMapper;

  TransactionPersistenceAdapter(
      TransactionFileJpaRepository transactionFileRepository,
      TransactionJpaRepository transactionRepository,
      TransactionMapper transactionMapper) {
    this.transactionFileRepository = transactionFileRepository;
    this.transactionRepository = transactionRepository;
    this.transactionMapper = transactionMapper;
  }

  @Override
  public List<TransactionFile> loadTransactionFiles(long userId) {
    return transactionFileRepository.findAllByUserId(userId).stream()
        .map(transactionMapper::toDomain)
        .toList();
  }

  @Override
  public Set<String> loadExistingBankReferences(long userId, Collection<String> bankReferences) {
    return Optional.of(bankReferences)
        .filter(Predicate.not(Collection::isEmpty))
        .map(references -> transactionRepository.findExistingBankReferences(userId, references))
        .orElseGet(Set::of);
  }

  @Override
  public void saveTransactionFile(
      long userId, TransactionFile file, List<CategorizedTransaction> transactions) {
    TransactionFileJpaEntity fileEntity =
        transactionFileRepository.save(transactionMapper.toEntity(userId, file));
    transactionRepository.saveAll(
        transactions.stream()
            .map(transaction -> transactionMapper.toEntity(userId, transaction, fileEntity))
            .toList());
  }

  @Override
  public List<StoredTransaction> loadTransactions(long userId, Optional<YearMonth> period) {
    return period
        .map(
            month ->
                transactionRepository.findAllOfThePeriod(
                    userId, month.atDay(1).atStartOfDay(), month.plusMonths(1).atDay(1).atStartOfDay()))
        .orElseGet(() -> transactionRepository.findAllByUserIdOrderByTransactionTimeAscIdAsc(userId))
        .stream()
        .map(transactionMapper::toDomain)
        .toList();
  }

  @Override
  public List<StoredTransaction> loadUncategorizedSpends(long userId) {
    return transactionRepository.findAllUncategorizedSpends(userId).stream()
        .map(transactionMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<StoredTransaction> updateTransactionCategory(
      long userId, long transactionId, Optional<Long> categoryId) {
    return transactionRepository
        .findByIdAndUserId(transactionId, userId)
        .map(
            entity -> {
              entity.setCategoryId(categoryId.orElse(null));
              return transactionRepository.save(entity);
            })
        .map(transactionMapper::toDomain);
  }

  @Override
  public void updateTransactionCategories(Map<Long, Long> categoryIdsByTransactionId) {
    transactionRepository.saveAll(
        transactionRepository.findAllById(categoryIdsByTransactionId.keySet()).stream()
            .peek(entity -> entity.setCategoryId(categoryIdsByTransactionId.get(entity.getId())))
            .toList());
  }
}
