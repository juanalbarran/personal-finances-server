package com.jarsolutions.finances.transaction.application.domain.service;

import static java.util.function.Predicate.not;

import com.jarsolutions.finances.category.application.port.in.GetCategorizerUseCase;
import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.transaction.application.domain.exception.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.domain.model.CategorizedTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.Statement;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import com.jarsolutions.finances.transaction.application.port.in.LoadStoredTransactionFileUseCase;
import com.jarsolutions.finances.transaction.application.port.in.UploadTransactionFileUseCase;
import com.jarsolutions.finances.transaction.application.port.out.LoadBankReferencesPort;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionFilesPort;
import com.jarsolutions.finances.transaction.application.port.out.ParseTransactionFilePort;
import com.jarsolutions.finances.transaction.application.port.out.ReadStoredTransactionFilePort;
import com.jarsolutions.finances.transaction.application.port.out.SaveTransactionFilePort;
import com.jarsolutions.finances.transaction.application.port.out.StoreTransactionFilePort;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads one transaction file of a user. Only the transactions of the month of the file are stored,
 * and a transaction the user already has is not stored twice. The spends get the category the rules
 * of the user choose.
 */
@UseCase
@Transactional
class LoadTransactionFileService
    implements UploadTransactionFileUseCase, LoadStoredTransactionFileUseCase {

  private final GetUserUseCase getUserUseCase;
  private final GetCategorizerUseCase getCategorizerUseCase;
  private final ParseTransactionFilePort parseTransactionFilePort;
  private final ReadStoredTransactionFilePort readStoredTransactionFilePort;
  private final StoreTransactionFilePort storeTransactionFilePort;
  private final LoadTransactionFilesPort loadTransactionFilesPort;
  private final LoadBankReferencesPort loadBankReferencesPort;
  private final SaveTransactionFilePort saveTransactionFilePort;

  LoadTransactionFileService(
      GetUserUseCase getUserUseCase,
      GetCategorizerUseCase getCategorizerUseCase,
      ParseTransactionFilePort parseTransactionFilePort,
      ReadStoredTransactionFilePort readStoredTransactionFilePort,
      StoreTransactionFilePort storeTransactionFilePort,
      LoadTransactionFilesPort loadTransactionFilesPort,
      LoadBankReferencesPort loadBankReferencesPort,
      SaveTransactionFilePort saveTransactionFilePort) {
    this.getUserUseCase = getUserUseCase;
    this.getCategorizerUseCase = getCategorizerUseCase;
    this.parseTransactionFilePort = parseTransactionFilePort;
    this.readStoredTransactionFilePort = readStoredTransactionFilePort;
    this.storeTransactionFilePort = storeTransactionFilePort;
    this.loadTransactionFilesPort = loadTransactionFilesPort;
    this.loadBankReferencesPort = loadBankReferencesPort;
    this.saveTransactionFilePort = saveTransactionFilePort;
  }

  /** The month of the file is the month of the last day it was exported for. */
  @Override
  public TransactionFile uploadTransactionFile(String username, byte[] content) {
    User user = getUserUseCase.getUser(username);
    Statement statement = parseTransactionFilePort.parse(content);
    TransactionFileName name = new TransactionFileName(statement.period());
    return alreadyLoaded(user, name, content)
        .orElseGet(
            () -> {
              TransactionFile file = load(user, name, content, statement);
              // Last, so a file that could not be loaded is not left in the directory
              storeTransactionFilePort.storeTransactionFile(
                  new TransactionFileLocation(user.username(), name), content);
              return file;
            });
  }

  /** The month of the file is the one in its name, and its user the one of its directory. */
  @Override
  public TransactionFile loadStoredTransactionFile(TransactionFileLocation location) {
    User user = getUserUseCase.getUser(location.username());
    byte[] content = readStoredTransactionFilePort.readStoredTransactionFile(location);
    return alreadyLoaded(user, location.name(), content)
        .orElseGet(
            () -> load(user, location.name(), content, parseTransactionFilePort.parse(content)));
  }

  /** Loading the same file again changes nothing, another file for the same month is refused. */
  private Optional<TransactionFile> alreadyLoaded(
      User user, TransactionFileName name, byte[] content) {
    String checksum = checksumOf(content);
    return loadTransactionFilesPort.loadTransactionFiles(user.id()).stream()
        .filter(file -> file.name().equals(name))
        .findFirst()
        .map(
            file ->
                Optional.of(file)
                    .filter(loaded -> loaded.checksum().equals(checksum))
                    .orElseThrow(() -> new TransactionFileConflictException(name)));
  }

  private TransactionFile load(
      User user, TransactionFileName name, byte[] content, Statement statement) {
    List<Transaction> ofThePeriod = statement.transactionsOf(name.period());
    Set<String> existing =
        loadBankReferencesPort.loadExistingBankReferences(
            user.id(), ofThePeriod.stream().map(Transaction::bankReference).toList());
    Function<Transaction, Optional<Long>> categoryOf =
        TransactionCategorizer.with(getCategorizerUseCase.getCategorizer(user.id()));
    List<CategorizedTransaction> newTransactions =
        ofThePeriod.stream()
            .filter(not(transaction -> existing.contains(transaction.bankReference())))
            .map(
                transaction ->
                    new CategorizedTransaction(transaction, categoryOf.apply(transaction)))
            .toList();
    TransactionFile file =
        new TransactionFile(name, checksumOf(content), Instant.now(), newTransactions.size());
    saveTransactionFilePort.saveTransactionFile(user.id(), file, newTransactions);
    return file;
  }

  private static String checksumOf(byte[] content) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
