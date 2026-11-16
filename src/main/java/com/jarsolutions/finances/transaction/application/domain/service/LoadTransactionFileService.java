package com.jarsolutions.finances.transaction.application.domain.service;

import static java.util.function.Predicate.not;

import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.transaction.application.domain.model.Statement;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import com.jarsolutions.finances.transaction.application.port.in.LoadStoredTransactionFileUseCase;
import com.jarsolutions.finances.transaction.application.port.in.UploadTransactionFileUseCase;
import com.jarsolutions.finances.transaction.application.port.out.LoadBankReferencesPort;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionFilesPort;
import com.jarsolutions.finances.transaction.application.port.out.ParseTransactionFilePort;
import com.jarsolutions.finances.transaction.application.port.out.ReadStoredTransactionFilePort;
import com.jarsolutions.finances.transaction.application.port.out.SaveTransactionFilePort;
import com.jarsolutions.finances.transaction.application.port.out.StoreTransactionFilePort;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads one transaction file. Only the transactions of the month of the file are stored, and a
 * transaction the database already has is not stored twice.
 */
@UseCase
@Transactional
class LoadTransactionFileService
    implements UploadTransactionFileUseCase, LoadStoredTransactionFileUseCase {

  private final ParseTransactionFilePort parseTransactionFilePort;
  private final ReadStoredTransactionFilePort readStoredTransactionFilePort;
  private final StoreTransactionFilePort storeTransactionFilePort;
  private final LoadTransactionFilesPort loadTransactionFilesPort;
  private final LoadBankReferencesPort loadBankReferencesPort;
  private final SaveTransactionFilePort saveTransactionFilePort;

  LoadTransactionFileService(
      ParseTransactionFilePort parseTransactionFilePort,
      ReadStoredTransactionFilePort readStoredTransactionFilePort,
      StoreTransactionFilePort storeTransactionFilePort,
      LoadTransactionFilesPort loadTransactionFilesPort,
      LoadBankReferencesPort loadBankReferencesPort,
      SaveTransactionFilePort saveTransactionFilePort) {
    this.parseTransactionFilePort = parseTransactionFilePort;
    this.readStoredTransactionFilePort = readStoredTransactionFilePort;
    this.storeTransactionFilePort = storeTransactionFilePort;
    this.loadTransactionFilesPort = loadTransactionFilesPort;
    this.loadBankReferencesPort = loadBankReferencesPort;
    this.saveTransactionFilePort = saveTransactionFilePort;
  }

  /** The month of the file is the month of the last day it was exported for. */
  @Override
  public TransactionFile uploadTransactionFile(byte[] content) {
    Statement statement = parseTransactionFilePort.parse(content);
    TransactionFileName name = new TransactionFileName(statement.period());
    return alreadyLoaded(name, content)
        .orElseGet(
            () -> {
              TransactionFile file = load(name, content, statement);
              // Last, so a file that could not be loaded is not left in the directory
              storeTransactionFilePort.storeTransactionFile(name, content);
              return file;
            });
  }

  /** The month of the file is the one in its name. */
  @Override
  public TransactionFile loadStoredTransactionFile(TransactionFileName name) {
    byte[] content = readStoredTransactionFilePort.readStoredTransactionFile(name);
    return alreadyLoaded(name, content)
        .orElseGet(() -> load(name, content, parseTransactionFilePort.parse(content)));
  }

  /** Loading the same file again changes nothing, another file for the same month is refused. */
  private Optional<TransactionFile> alreadyLoaded(TransactionFileName name, byte[] content) {
    String checksum = checksumOf(content);
    return loadTransactionFilesPort.loadTransactionFiles().stream()
        .filter(file -> file.name().equals(name))
        .findFirst()
        .map(
            file ->
                Optional.of(file)
                    .filter(loaded -> loaded.checksum().equals(checksum))
                    .orElseThrow(() -> new TransactionFileConflictException(name)));
  }

  private TransactionFile load(TransactionFileName name, byte[] content, Statement statement) {
    List<Transaction> ofThePeriod = statement.transactionsOf(name.period());
    Set<String> existing =
        loadBankReferencesPort.loadExistingBankReferences(
            ofThePeriod.stream().map(Transaction::bankReference).toList());
    List<Transaction> newTransactions =
        ofThePeriod.stream()
            .filter(not(transaction -> existing.contains(transaction.bankReference())))
            .toList();
    TransactionFile file =
        new TransactionFile(name, checksumOf(content), Instant.now(), newTransactions.size());
    saveTransactionFilePort.saveTransactionFile(file, newTransactions);
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
