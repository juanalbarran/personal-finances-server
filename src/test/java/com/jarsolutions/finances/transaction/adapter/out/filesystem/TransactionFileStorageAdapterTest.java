package com.jarsolutions.finances.transaction.adapter.out.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jarsolutions.finances.transaction.application.domain.exception.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TransactionFileStorageAdapterTest {

  private static final TransactionFileLocation OCTOBER =
      new TransactionFileLocation("juan", new TransactionFileName(YearMonth.of(2026, 10)));

  @TempDir Path directory;

  @Test
  void list_returnsOnlyTheFilesOfTheUserDirectoriesThatFollowTheNamingPattern() throws IOException {
    Path juan = Files.createDirectory(directory.resolve("juan"));
    Path ana = Files.createDirectory(directory.resolve("ana"));
    Files.write(juan.resolve("2026.october.transactions.xlsx"), new byte[] {1});
    Files.write(juan.resolve("transactions.november.2026.xlsx"), new byte[] {1});
    Files.createDirectory(juan.resolve("2026.december.transactions.xlsx"));
    Files.write(ana.resolve("2026.october.transactions.xlsx"), new byte[] {1});
    // Not in the directory of a user
    Files.write(directory.resolve("2026.september.transactions.xlsx"), new byte[] {1});

    assertThat(adapter(directory).listStoredTransactionFiles())
        .containsExactly(new TransactionFileLocation("ana", OCTOBER.name()), OCTOBER);
  }

  @Test
  void list_whenTheDirectoryDoesNotExist_returnsNothing() {
    assertThat(adapter(directory.resolve("missing")).listStoredTransactionFiles()).isEmpty();
  }

  @Test
  void store_createsTheDirectoryAndWritesTheFileUnderItsName() throws IOException {
    Path files = directory.resolve("files");

    adapter(files).storeTransactionFile(OCTOBER, new byte[] {1, 2, 3});

    assertThat(Files.readAllBytes(files.resolve("juan/2026.october.transactions.xlsx")))
        .containsExactly(1, 2, 3);
    assertThat(adapter(files).readStoredTransactionFile(OCTOBER)).containsExactly(1, 2, 3);
  }

  @Test
  void store_whenTheSameFileIsAlreadyThere_doesNothing() {
    adapter(directory).storeTransactionFile(OCTOBER, new byte[] {1, 2, 3});

    adapter(directory).storeTransactionFile(OCTOBER, new byte[] {1, 2, 3});

    assertThat(adapter(directory).readStoredTransactionFile(OCTOBER)).containsExactly(1, 2, 3);
  }

  @Test
  void store_whenAnotherFileIsThereForTheMonth_refusesAndKeepsIt() {
    adapter(directory).storeTransactionFile(OCTOBER, new byte[] {1, 2, 3});

    assertThatThrownBy(() -> adapter(directory).storeTransactionFile(OCTOBER, new byte[] {9}))
        .isInstanceOf(TransactionFileConflictException.class);
    assertThat(adapter(directory).readStoredTransactionFile(OCTOBER)).containsExactly(1, 2, 3);
  }

  @Test
  void aUsernameThatLeavesTheFilesDirectoryIsRefused() {
    TransactionFileLocation outside = new TransactionFileLocation("..", OCTOBER.name());

    assertThatThrownBy(() -> adapter(directory).storeTransactionFile(outside, new byte[] {1}))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> adapter(directory).readStoredTransactionFile(outside))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static TransactionFileStorageAdapter adapter(Path directory) {
    return new TransactionFileStorageAdapter(directory);
  }
}
