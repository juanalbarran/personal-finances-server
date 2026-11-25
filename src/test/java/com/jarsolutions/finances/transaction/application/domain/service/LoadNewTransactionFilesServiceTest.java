package com.jarsolutions.finances.transaction.application.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jarsolutions.finances.transaction.application.domain.exception.InvalidTransactionFileException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import com.jarsolutions.finances.user.application.domain.exception.UserNotFoundException;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class LoadNewTransactionFilesServiceTest {

  private static final TransactionFileName AUGUST = new TransactionFileName(YearMonth.of(2026, 8));
  private static final TransactionFileName SEPTEMBER = new TransactionFileName(YearMonth.of(2026, 9));
  private static final TransactionFileName OCTOBER = new TransactionFileName(YearMonth.of(2026, 10));
  private static final Map<String, User> USERS =
      Map.of(
          "ana", new User(1, "ana", "ana@example.com", "Ana", "A"),
          "juan", new User(2, "juan", "juan@example.com", "Juan", "J"));
  private static final GetUserUseCase GET_USER =
      username ->
          Optional.ofNullable(USERS.get(username))
              .orElseThrow(() -> new UserNotFoundException(username));

  private final List<TransactionFileLocation> loads = new ArrayList<>();

  @Test
  void loadNew_loadsTheFilesEachUserDidNotLoadBefore_theOldestMonthFirst() {
    LoadNewTransactionFilesService service =
        new LoadNewTransactionFilesService(
            () ->
                List.of(
                    of("juan", OCTOBER),
                    of("juan", AUGUST),
                    of("juan", SEPTEMBER),
                    of("ana", SEPTEMBER)),
            // Only juan loaded September before
            userId -> userId == 2 ? List.of(loaded(SEPTEMBER)) : List.of(),
            GET_USER,
            this::load);

    assertThat(service.loadNewTransactionFiles()).hasSize(3);
    assertThat(loads)
        .containsExactly(of("ana", SEPTEMBER), of("juan", AUGUST), of("juan", OCTOBER));
  }

  @Test
  void loadNew_whenAFileFails_skipsItAndLoadsTheOthers() {
    LoadNewTransactionFilesService service =
        new LoadNewTransactionFilesService(
            () -> List.of(of("juan", AUGUST), of("juan", OCTOBER)),
            userId -> List.of(),
            GET_USER,
            location ->
                Optional.of(location)
                    .filter(Predicate.not(of("juan", AUGUST)::equals))
                    .map(this::load)
                    .orElseThrow(() -> new InvalidTransactionFileException("broken")));

    assertThat(service.loadNewTransactionFiles())
        .extracting(TransactionFile::name)
        .containsExactly(OCTOBER);
  }

  @Test
  void loadNew_skipsTheDirectoriesThatAreNotNamedAfterAUser() {
    LoadNewTransactionFilesService service =
        new LoadNewTransactionFilesService(
            () -> List.of(of("nobody", AUGUST), of("juan", OCTOBER)),
            userId -> List.of(),
            GET_USER,
            this::load);

    service.loadNewTransactionFiles();

    assertThat(loads).containsExactly(of("juan", OCTOBER));
  }

  private TransactionFile load(TransactionFileLocation location) {
    loads.add(location);
    return loaded(location.name());
  }

  private static TransactionFileLocation of(String username, TransactionFileName name) {
    return new TransactionFileLocation(username, name);
  }

  private static TransactionFile loaded(TransactionFileName name) {
    return new TransactionFile(name, "checksum", Instant.EPOCH, 0);
  }
}
