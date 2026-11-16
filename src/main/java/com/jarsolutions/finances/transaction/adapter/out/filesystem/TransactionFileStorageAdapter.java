package com.jarsolutions.finances.transaction.adapter.out.filesystem;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import com.jarsolutions.finances.transaction.application.port.out.ListStoredTransactionFilesPort;
import com.jarsolutions.finances.transaction.application.port.out.ReadStoredTransactionFilePort;
import com.jarsolutions.finances.transaction.application.port.out.StoreTransactionFilePort;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Keeps the transaction files in the files directory, {@code docs/files} unless configured. */
@Component
class TransactionFileStorageAdapter
    implements ListStoredTransactionFilesPort, ReadStoredTransactionFilePort, StoreTransactionFilePort {

  private final Path directory;

  TransactionFileStorageAdapter(@Value("${finances.transaction-files.directory}") Path directory) {
    this.directory = directory;
  }

  /** Files that do not follow the naming pattern are ignored. */
  @Override
  public List<TransactionFileName> listStoredTransactionFiles() {
    if (!Files.isDirectory(directory)) {
      return List.of();
    }
    try (Stream<Path> files = Files.list(directory)) {
      return files
          .filter(Files::isRegularFile)
          .map(file -> file.getFileName().toString())
          .map(TransactionFileName::parse)
          .flatMap(Optional::stream)
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public byte[] readStoredTransactionFile(TransactionFileName name) {
    try {
      return Files.readAllBytes(pathOf(name));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void storeTransactionFile(TransactionFileName name, byte[] content) {
    Path file = pathOf(name);
    try {
      if (Files.exists(file)) {
        if (!Arrays.equals(Files.readAllBytes(file), content)) {
          throw new TransactionFileConflictException(name);
        }
        return;
      }
      Files.createDirectories(directory);
      Files.write(file, content, StandardOpenOption.CREATE_NEW);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Path pathOf(TransactionFileName name) {
    return directory.resolve(name.value());
  }
}
