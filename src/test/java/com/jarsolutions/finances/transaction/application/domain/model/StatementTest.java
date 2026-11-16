package com.jarsolutions.finances.transaction.application.domain.model;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatementTest {

  @Test
  void transactionsOf_keepsOnlyTheOnesThatHappenedInTheMonth() {
    Statement statement =
        new Statement(
            LocalDate.of(2026, 10, 31),
            List.of(
                // Booked in October, but it happened in September
                purchase("september", "2026-09-30T18:57:28", "-1200"),
                purchase("first", "2026-10-01T00:00:00", "-100"),
                // Booked in November, but it happened in October
                purchase("last", "2026-10-31T23:59:59", "-200"),
                purchase("november", "2026-11-01T00:00:00", "-300")));

    assertThat(statement.period()).isEqualTo(YearMonth.of(2026, 10));
    assertThat(statement.transactionsOf(YearMonth.of(2026, 10)))
        .extracting(Transaction::bankReference)
        .containsExactly("first", "last");
  }
}
