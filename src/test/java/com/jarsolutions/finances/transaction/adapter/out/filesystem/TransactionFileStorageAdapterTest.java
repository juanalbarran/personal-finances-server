package com.jarsolutions.finances.transaction.adapter.out.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TransactionFileStorageAdapterTest {

  private static final TransactionFileName OCTOBER = new TransactionFileName(YearMonth.of(2026, 10));

  @TempDir Path directory;

  @Test
  void list_returnsOnlyTheFilesThatFollowTheNamingPattern() throws IOException {
    Files.write(directory.resolve("2026.october.transactions.xlsx"), new byte[] {1});
    Files.write(directory.resolve("transactions.november.2026.xlsx"), new byte[] {1});
    Files.createDirectory(directory.resolve("2026.december.transactions.xlsx"));

    assertThat(adapter(directory).listStoredTransactionFiles()).containsExactly(OCTOBER);
  }

  @Test
  void list_whenTheDirectoryDoesNotExist_returnsNothing() {
    assertThat(adapter(directory.resolve("missing")).listStoredTransactionFiles()).isEmpty();
  }

  @Test
  void store_createsTheDirectoryAndWritesTheFileUnderItsName() throws IOException {
    Path files = directory.resolve("files");

    adapter(files).storeTransactionFile(OCTOBER, new byte[] {1, 2, 3});

    assertThat(Files.readAllBytes(files.resolve("2026.october.transactions.xlsx")))
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

  private static TransactionFileStorageAdapter adapter(Path directory) {
    return new TransactionFileStorageAdapter(directory);
  }
}
