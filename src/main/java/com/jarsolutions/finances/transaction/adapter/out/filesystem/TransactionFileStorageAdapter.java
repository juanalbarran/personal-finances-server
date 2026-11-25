package com.jarsolutions.finances.transaction.adapter.out.filesystem;

import com.jarsolutions.finances.transaction.application.domain.exception.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;
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

/**
 * Keeps the transaction files in the files directory, {@code docs/files} unless configured, with a
 * directory per user named after the username.
 */
@Component
class TransactionFileStorageAdapter
    implements ListStoredTransactionFilesPort, ReadStoredTransactionFilePort, StoreTransactionFilePort {

  private final Path directory;

  TransactionFileStorageAdapter(@Value("${finances.transaction-files.directory}") Path directory) {
    this.directory = directory;
  }

  /** Files that do not follow the naming pattern or are not in a user directory are ignored. */
  @Override
  public List<TransactionFileLocation> listStoredTransactionFiles() {
    return list(directory)
        .filter(Files::isDirectory)
        .flatMap(
            userDirectory ->
                list(userDirectory)
                    .filter(Files::isRegularFile)
                    .map(file -> file.getFileName().toString())
                    .map(TransactionFileName::parse)
                    .flatMap(Optional::stream)
                    .map(
                        name ->
                            new TransactionFileLocation(
                                userDirectory.getFileName().toString(), name)))
        .toList();
  }

  @Override
  public byte[] readStoredTransactionFile(TransactionFileLocation location) {
    try {
      return Files.readAllBytes(pathOf(location));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void storeTransactionFile(TransactionFileLocation location, byte[] content) {
    Path file = pathOf(location);
    try {
      if (Files.exists(file)) {
        if (!Arrays.equals(Files.readAllBytes(file), content)) {
          throw new TransactionFileConflictException(location.name());
        }
        return;
      }
      Files.createDirectories(file.getParent());
      Files.write(file, content, StandardOpenOption.CREATE_NEW);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** What the directory has in it, nothing when it does not exist. */
  private static Stream<Path> list(Path directory) {
    if (!Files.isDirectory(directory)) {
      return Stream.empty();
    }
    try (Stream<Path> content = Files.list(directory)) {
      return content.sorted().toList().stream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** A username never takes the file out of the files directory. */
  private Path pathOf(TransactionFileLocation location) {
    Path userDirectory = directory.resolve(location.username()).normalize();
    if (!directory.normalize().equals(userDirectory.getParent())) {
      throw new IllegalArgumentException(
          "%s is not a directory of the files directory".formatted(location.username()));
    }
    return userDirectory.resolve(location.name().value());
  }
}
