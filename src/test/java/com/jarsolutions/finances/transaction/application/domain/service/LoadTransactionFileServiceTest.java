package com.jarsolutions.finances.transaction.application.domain.service;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jarsolutions.finances.transaction.application.domain.model.Statement;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class LoadTransactionFileServiceTest {

  private static final TransactionFileName OCTOBER = new TransactionFileName(YearMonth.of(2026, 10));
  private static final byte[] CONTENT = "october".getBytes(StandardCharsets.UTF_8);
  private static final Statement STATEMENT =
      new Statement(
          LocalDate.of(2026, 10, 31),
          List.of(
              purchase("september", "2026-09-30T18:57:28", "-1200"),
              purchase("october-1", "2026-10-01T10:00:00", "-100"),
              purchase("october-2", "2026-10-31T19:05:25", "-200")));

  // The database, the files directory and the parser, in memory
  private final List<TransactionFile> savedFiles = new ArrayList<>();
  private final Set<String> savedBankReferences = new HashSet<>();
  private final Map<TransactionFileName, byte[]> directory = new HashMap<>();
  private final Map<String, Statement> statements = new HashMap<>(Map.of("october", STATEMENT));

  private final LoadTransactionFileService service =
      new LoadTransactionFileService(
          content -> statements.get(new String(content, StandardCharsets.UTF_8)),
          directory::get,
          directory::put,
          () -> List.copyOf(savedFiles),
          references ->
              references.stream().filter(savedBankReferences::contains).collect(Collectors.toSet()),
          (file, transactions) -> {
            savedFiles.add(file);
            transactions.stream().map(Transaction::bankReference).forEach(savedBankReferences::add);
          });

  @Test
  void upload_savesOnlyTheTransactionsOfTheMonthAndStoresTheFileUnderItsName() {
    TransactionFile file = service.uploadTransactionFile(CONTENT);

    assertThat(file.name()).isEqualTo(OCTOBER);
    assertThat(file.transactionCount()).isEqualTo(2);
    assertThat(file.checksum()).hasSize(64);
    assertThat(savedFiles).containsExactly(file);
    assertThat(savedBankReferences).containsExactlyInAnyOrder("october-1", "october-2");
    assertThat(directory).containsEntry(OCTOBER, CONTENT);
  }

  @Test
  void upload_whenTheSameFileWasLoaded_returnsThatLoadAndSavesNothing() {
    TransactionFile first = service.uploadTransactionFile(CONTENT);

    assertThat(service.uploadTransactionFile(CONTENT)).isEqualTo(first);
    assertThat(savedFiles).hasSize(1);
  }

  @Test
  void upload_whenAnotherFileWasLoadedForTheMonth_refuses() {
    service.uploadTransactionFile(CONTENT);
    statements.put("another october", STATEMENT);

    assertThatThrownBy(
            () -> service.uploadTransactionFile("another october".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(TransactionFileConflictException.class);
    assertThat(savedFiles).hasSize(1);
    assertThat(directory).containsEntry(OCTOBER, CONTENT);
  }

  @Test
  void upload_doesNotSaveATransactionTheDatabaseAlreadyHas() {
    savedBankReferences.add("october-1");

    assertThat(service.uploadTransactionFile(CONTENT).transactionCount()).isEqualTo(1);
  }

  @Test
  void loadStored_takesTheMonthFromTheNameOfTheFile() {
    TransactionFileName september = new TransactionFileName(YearMonth.of(2026, 9));
    directory.put(september, CONTENT);

    TransactionFile file = service.loadStoredTransactionFile(september);

    assertThat(file.name()).isEqualTo(september);
    assertThat(savedBankReferences).containsExactly("september");
  }
}
