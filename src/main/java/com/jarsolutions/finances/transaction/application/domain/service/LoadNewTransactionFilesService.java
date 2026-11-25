package com.jarsolutions.finances.transaction.application.domain.service;

import static java.util.function.Predicate.not;

import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import com.jarsolutions.finances.transaction.application.port.in.LoadNewTransactionFilesUseCase;
import com.jarsolutions.finances.transaction.application.port.in.LoadStoredTransactionFileUseCase;
import com.jarsolutions.finances.transaction.application.port.out.ListStoredTransactionFilesPort;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionFilesPort;
import com.jarsolutions.finances.user.application.domain.exception.UserNotFoundException;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the files of the files directory that were not loaded before, user by user and the oldest
 * month first. A file that fails is logged and skipped, so it does not stop the others, and so is
 * a directory that is not named after a user.
 */
@UseCase
class LoadNewTransactionFilesService implements LoadNewTransactionFilesUseCase {

  private static final Logger log = LoggerFactory.getLogger(LoadNewTransactionFilesService.class);

  private final ListStoredTransactionFilesPort listStoredTransactionFilesPort;
  private final LoadTransactionFilesPort loadTransactionFilesPort;
  private final GetUserUseCase getUserUseCase;
  // Each file is loaded through the use case, in its own database transaction
  private final LoadStoredTransactionFileUseCase loadStoredTransactionFileUseCase;

  LoadNewTransactionFilesService(
      ListStoredTransactionFilesPort listStoredTransactionFilesPort,
      LoadTransactionFilesPort loadTransactionFilesPort,
      GetUserUseCase getUserUseCase,
      LoadStoredTransactionFileUseCase loadStoredTransactionFileUseCase) {
    this.listStoredTransactionFilesPort = listStoredTransactionFilesPort;
    this.loadTransactionFilesPort = loadTransactionFilesPort;
    this.getUserUseCase = getUserUseCase;
    this.loadStoredTransactionFileUseCase = loadStoredTransactionFileUseCase;
  }

  @Override
  public List<TransactionFile> loadNewTransactionFiles() {
    return listStoredTransactionFilesPort.listStoredTransactionFiles().stream()
        .collect(
            Collectors.groupingBy(
                TransactionFileLocation::username, TreeMap::new, Collectors.toList()))
        .entrySet()
        .stream()
        .flatMap(files -> loadNewOf(files.getKey(), files.getValue()))
        .toList();
  }

  private Stream<TransactionFile> loadNewOf(String username, List<TransactionFileLocation> stored) {
    try {
      Set<TransactionFileName> loaded =
          loadTransactionFilesPort
              .loadTransactionFiles(getUserUseCase.getUser(username).id())
              .stream()
              .map(TransactionFile::name)
              .collect(Collectors.toSet());
      return stored.stream()
          .filter(not(location -> loaded.contains(location.name())))
          .sorted(Comparator.comparing(location -> location.name().period()))
          .map(this::load)
          .flatMap(Optional::stream);
    } catch (UserNotFoundException e) {
      log.warn("Skipped the files of the directory {}, no user has that username", username);
      return Stream.empty();
    }
  }

  private Optional<TransactionFile> load(TransactionFileLocation location) {
    try {
      TransactionFile file = loadStoredTransactionFileUseCase.loadStoredTransactionFile(location);
      log.info("Loaded {} transactions from {}", file.transactionCount(), location.value());
      return Optional.of(file);
    } catch (RuntimeException e) {
      log.error("Could not load {}", location.value(), e);
      return Optional.empty();
    }
  }
}
