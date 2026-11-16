package com.jarsolutions.finances.transaction.application.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TransactionFileNameTest {

  @Test
  void value_isYearMonthNameTransactionsXlsx() {
    assertThat(new TransactionFileName(YearMonth.of(2026, 10)).value())
        .isEqualTo("2026.october.transactions.xlsx");
  }

  @Test
  void parse_readsThePeriodFromTheName() {
    assertThat(TransactionFileName.parse("2026.october.transactions.xlsx"))
        .map(TransactionFileName::period)
        .contains(YearMonth.of(2026, 10));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "transactions.november.2026.xlsx",
        "2026.10.transactions.xlsx",
        "2026.octubre.transactions.xlsx",
        "2026.October.transactions.xlsx",
        "2026.october.transactions.xls",
        "notes.txt"
      })
  void parse_whenTheNameDoesNotFollowThePattern_returnsEmpty(String fileName) {
    assertThat(TransactionFileName.parse(fileName)).isEmpty();
  }
}
