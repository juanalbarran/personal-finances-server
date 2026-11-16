package com.jarsolutions.finances.transaction.application.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jarsolutions.finances.transaction.application.domain.model.InvalidTransactionFileException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class LoadNewTransactionFilesServiceTest {

  private static final TransactionFileName AUGUST = new TransactionFileName(YearMonth.of(2026, 8));
  private static final TransactionFileName SEPTEMBER = new TransactionFileName(YearMonth.of(2026, 9));
  private static final TransactionFileName OCTOBER = new TransactionFileName(YearMonth.of(2026, 10));

  @Test
  void loadNew_loadsTheFilesThatWereNotLoadedBefore_theOldestMonthFirst() {
    LoadNewTransactionFilesService service =
        new LoadNewTransactionFilesService(
            () -> List.of(OCTOBER, AUGUST, SEPTEMBER),
            () -> List.of(loaded(SEPTEMBER)),
            LoadNewTransactionFilesServiceTest::loaded);

    assertThat(service.loadNewTransactionFiles())
        .extracting(TransactionFile::name)
        .containsExactly(AUGUST, OCTOBER);
  }

  @Test
  void loadNew_whenAFileFails_skipsItAndLoadsTheOthers() {
    LoadNewTransactionFilesService service =
        new LoadNewTransactionFilesService(
            () -> List.of(AUGUST, OCTOBER),
            List::of,
            name ->
                Optional.of(name)
                    .filter(Predicate.not(AUGUST::equals))
                    .map(LoadNewTransactionFilesServiceTest::loaded)
                    .orElseThrow(() -> new InvalidTransactionFileException("broken")));

    assertThat(service.loadNewTransactionFiles())
        .extracting(TransactionFile::name)
        .containsExactly(OCTOBER);
  }

  private static TransactionFile loaded(TransactionFileName name) {
    return new TransactionFile(name, "checksum", Instant.EPOCH, 0);
  }
}
