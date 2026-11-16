package com.jarsolutions.finances.transaction.adapter.out.persistence;

import com.jarsolutions.finances.common.PersistenceAdapter;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.port.out.LoadBankReferencesPort;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionFilesPort;
import com.jarsolutions.finances.transaction.application.port.out.SaveTransactionFilePort;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@PersistenceAdapter
class TransactionPersistenceAdapter
    implements LoadTransactionFilesPort, LoadBankReferencesPort, SaveTransactionFilePort {

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
  public List<TransactionFile> loadTransactionFiles() {
    return transactionFileRepository.findAll().stream().map(transactionMapper::toDomain).toList();
  }

  @Override
  public Set<String> loadExistingBankReferences(Collection<String> bankReferences) {
    return Optional.of(bankReferences)
        .filter(Predicate.not(Collection::isEmpty))
        .map(transactionRepository::findExistingBankReferences)
        .orElseGet(Set::of);
  }

  @Override
  public void saveTransactionFile(TransactionFile file, List<Transaction> transactions) {
    TransactionFileJpaEntity fileEntity =
        transactionFileRepository.save(transactionMapper.toEntity(file));
    transactionRepository.saveAll(
        transactions.stream()
            .map(transaction -> transactionMapper.toEntity(transaction, fileEntity))
            .toList());
  }
}
