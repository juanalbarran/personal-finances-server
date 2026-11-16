package com.jarsolutions.finances.transaction.application.domain.service;

import static java.util.function.Predicate.not;

import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import com.jarsolutions.finances.transaction.application.port.in.LoadNewTransactionFilesUseCase;
import com.jarsolutions.finances.transaction.application.port.in.LoadStoredTransactionFileUseCase;
import com.jarsolutions.finances.transaction.application.port.out.ListStoredTransactionFilesPort;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionFilesPort;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the files of the files directory that were not loaded before, the oldest month first. A
 * file that fails is logged and skipped, so it does not stop the others.
 */
@UseCase
class LoadNewTransactionFilesService implements LoadNewTransactionFilesUseCase {

  private static final Logger log = LoggerFactory.getLogger(LoadNewTransactionFilesService.class);

  private final ListStoredTransactionFilesPort listStoredTransactionFilesPort;
  private final LoadTransactionFilesPort loadTransactionFilesPort;
  // Each file is loaded through the use case, in its own database transaction
  private final LoadStoredTransactionFileUseCase loadStoredTransactionFileUseCase;

  LoadNewTransactionFilesService(
      ListStoredTransactionFilesPort listStoredTransactionFilesPort,
      LoadTransactionFilesPort loadTransactionFilesPort,
      LoadStoredTransactionFileUseCase loadStoredTransactionFileUseCase) {
    this.listStoredTransactionFilesPort = listStoredTransactionFilesPort;
    this.loadTransactionFilesPort = loadTransactionFilesPort;
    this.loadStoredTransactionFileUseCase = loadStoredTransactionFileUseCase;
  }

  @Override
  public List<TransactionFile> loadNewTransactionFiles() {
    Set<TransactionFileName> loaded =
        loadTransactionFilesPort.loadTransactionFiles().stream()
            .map(TransactionFile::name)
            .collect(Collectors.toSet());
    return listStoredTransactionFilesPort.listStoredTransactionFiles().stream()
        .filter(not(loaded::contains))
        .sorted(Comparator.comparing(TransactionFileName::period))
        .map(this::load)
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<TransactionFile> load(TransactionFileName name) {
    try {
      TransactionFile file = loadStoredTransactionFileUseCase.loadStoredTransactionFile(name);
      log.info("Loaded {} transactions from {}", file.transactionCount(), name.value());
      return Optional.of(file);
    } catch (RuntimeException e) {
      log.error("Could not load {}", name.value(), e);
      return Optional.empty();
    }
  }
}
