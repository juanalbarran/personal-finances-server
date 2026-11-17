package com.jarsolutions.finances.transaction.application.domain.service;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static com.jarsolutions.finances.transaction.TransactionFileFixture.transfer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jarsolutions.finances.category.application.domain.model.Categorizer;
import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.RuleField;
import com.jarsolutions.finances.transaction.application.domain.model.CategorizedTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.Statement;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.domain.model.UserNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class LoadTransactionFileServiceTest {

  private static final User JUAN = new User(7, "juan", "juan@example.com", "Juan", "J");
  private static final long GROCERIES = 20;
  private static final TransactionFileName OCTOBER = new TransactionFileName(YearMonth.of(2026, 10));
  private static final TransactionFileLocation JUAN_OCTOBER =
      new TransactionFileLocation("juan", OCTOBER);
  private static final byte[] CONTENT = "october".getBytes(StandardCharsets.UTF_8);
  private static final Statement STATEMENT =
      new Statement(
          LocalDate.of(2026, 10, 31),
          List.of(
              purchase("september", "2026-09-30T18:57:28", "-1200"),
              purchase("october-1", "2026-10-01T10:00:00", "-100"),
              transfer("october-2", "2026-10-31T19:05:25", "200")));

  // The database, the files directory and the parser, in memory
  private final List<TransactionFile> savedFiles = new ArrayList<>();
  private final List<CategorizedTransaction> savedTransactions = new ArrayList<>();
  private final Set<String> savedBankReferences = new HashSet<>();
  private final Map<TransactionFileLocation, byte[]> directory = new HashMap<>();
  private final Map<String, Statement> statements = new HashMap<>(Map.of("october", STATEMENT));

  private final LoadTransactionFileService service =
      new LoadTransactionFileService(
          username ->
              Optional.of(JUAN)
                  .filter(user -> user.username().equals(username))
                  .orElseThrow(() -> new UserNotFoundException(username)),
          // The purchases of the fixture are groceries for the bank
          userId ->
              new Categorizer(
                  List.of(
                      new CategoryRule(
                          1, userId, GROCERIES, RuleField.BANK_CATEGORY, "Bevásárlás"))),
          content -> statements.get(new String(content, StandardCharsets.UTF_8)),
          directory::get,
          directory::put,
          userId -> List.copyOf(savedFiles),
          (userId, references) ->
              references.stream().filter(savedBankReferences::contains).collect(Collectors.toSet()),
          (userId, file, transactions) -> {
            assertThat(userId).isEqualTo(JUAN.id());
            savedFiles.add(file);
            savedTransactions.addAll(transactions);
            transactions.stream()
                .map(categorized -> categorized.transaction().bankReference())
                .forEach(savedBankReferences::add);
          });

  @Test
  void upload_savesOnlyTheTransactionsOfTheMonthAndStoresTheFileInTheDirectoryOfTheUser() {
    TransactionFile file = service.uploadTransactionFile("juan", CONTENT);

    assertThat(file.name()).isEqualTo(OCTOBER);
    assertThat(file.transactionCount()).isEqualTo(2);
    assertThat(file.checksum()).hasSize(64);
    assertThat(savedFiles).containsExactly(file);
    assertThat(savedBankReferences).containsExactlyInAnyOrder("october-1", "october-2");
    assertThat(directory).containsEntry(JUAN_OCTOBER, CONTENT);
  }

  @Test
  void upload_putsTheSpendsInTheCategoryOfTheRulesAndLeavesTheIncomeWithout() {
    service.uploadTransactionFile("juan", CONTENT);

    assertThat(savedTransactions)
        .extracting(
            categorized -> categorized.transaction().bankReference(),
            CategorizedTransaction::categoryId)
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple("october-1", Optional.of(GROCERIES)),
            org.assertj.core.groups.Tuple.tuple("october-2", Optional.empty()));
  }

  @Test
  void upload_whenTheUserDoesNotExist_refusesAndStoresNothing() {
    assertThatThrownBy(() -> service.uploadTransactionFile("nobody", CONTENT))
        .isInstanceOf(UserNotFoundException.class);
    assertThat(savedFiles).isEmpty();
    assertThat(directory).isEmpty();
  }

  @Test
  void upload_whenTheSameFileWasLoaded_returnsThatLoadAndSavesNothing() {
    TransactionFile first = service.uploadTransactionFile("juan", CONTENT);

    assertThat(service.uploadTransactionFile("juan", CONTENT)).isEqualTo(first);
    assertThat(savedFiles).hasSize(1);
  }

  @Test
  void upload_whenAnotherFileWasLoadedForTheMonth_refuses() {
    service.uploadTransactionFile("juan", CONTENT);
    statements.put("another october", STATEMENT);

    assertThatThrownBy(
            () ->
                service.uploadTransactionFile(
                    "juan", "another october".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(TransactionFileConflictException.class);
    assertThat(savedFiles).hasSize(1);
    assertThat(directory).containsEntry(JUAN_OCTOBER, CONTENT);
  }

  @Test
  void upload_doesNotSaveATransactionTheDatabaseAlreadyHas() {
    savedBankReferences.add("october-1");

    assertThat(service.uploadTransactionFile("juan", CONTENT).transactionCount()).isEqualTo(1);
  }

  @Test
  void loadStored_takesTheMonthFromTheNameOfTheFile() {
    TransactionFileLocation september =
        new TransactionFileLocation("juan", new TransactionFileName(YearMonth.of(2026, 9)));
    directory.put(september, CONTENT);

    TransactionFile file = service.loadStoredTransactionFile(september);

    assertThat(file.name()).isEqualTo(september.name());
    assertThat(savedBankReferences).containsExactly("september");
  }
}
