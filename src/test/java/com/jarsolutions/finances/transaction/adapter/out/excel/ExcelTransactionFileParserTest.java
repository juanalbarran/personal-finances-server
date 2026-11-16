package com.jarsolutions.finances.transaction.adapter.out.excel;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static com.jarsolutions.finances.transaction.TransactionFileFixture.transfer;
import static com.jarsolutions.finances.transaction.TransactionFileFixture.xlsx;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jarsolutions.finances.transaction.application.domain.model.InvalidTransactionFileException;
import com.jarsolutions.finances.transaction.application.domain.model.Statement;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ExcelTransactionFileParserTest {

  private final ExcelTransactionFileParser parser = new ExcelTransactionFileParser();

  @Test
  void parse_readsTheQueryEndAndEveryTransaction() {
    // The amount is a number cell, which is read with one decimal
    Transaction purchase = purchase("202610311905002717", "2026-10-31T19:05:25", "-845.0");
    Transaction salary = transfer("202610071347002652", "2026-10-07T13:47:00", "1046823.0");

    Statement statement = parser.parse(xlsx(LocalDate.of(2026, 10, 31), List.of(purchase, salary)));

    assertThat(statement.queryEnd()).isEqualTo(LocalDate.of(2026, 10, 31));
    assertThat(statement.transactions()).containsExactly(purchase, salary);
  }

  @Test
  void parse_whenTheContentIsNotAnExcelFile_throws() {
    assertThatThrownBy(() -> parser.parse("not an excel".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(InvalidTransactionFileException.class);
  }

  @Test
  void parse_whenTheExcelIsNotATransactionFile_throws() throws IOException {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      workbook.createSheet("other").createRow(0).createCell(0).setCellValue("something else");
      workbook.write(out);

      assertThatThrownBy(() -> parser.parse(out.toByteArray()))
          .isInstanceOf(InvalidTransactionFileException.class)
          .hasMessageContaining("header");
    }
  }
}
