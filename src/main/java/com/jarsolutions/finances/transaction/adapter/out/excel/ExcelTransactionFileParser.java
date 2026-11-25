package com.jarsolutions.finances.transaction.adapter.out.excel;

import com.jarsolutions.finances.transaction.application.domain.exception.InvalidTransactionFileException;
import com.jarsolutions.finances.transaction.application.domain.model.Statement;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import com.jarsolutions.finances.transaction.application.port.out.ParseTransactionFilePort;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

/**
 * Reads the account history the bank exports as {@code .xlsx}: some lines about the export, then a
 * header line and one line per transaction. The columns are found by their header, not by their
 * position.
 */
@Component
class ExcelTransactionFileParser implements ParseTransactionFilePort {

  private static final String QUERY_END_LABEL = "Lekérdezés vége";
  private static final DateTimeFormatter TRANSACTION_TIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /** The columns of the file, with the header the bank gives them. */
  private enum Column {
    ACCOUNT_NUMBER("Számlaszám"),
    COUNTERPARTY_ACCOUNT_NUMBER("Ellenoldali számlaszám"),
    COUNTERPARTY_NAME("Ellenoldali név"),
    TYPE("Forgalom típusa"),
    MEMO("Közlemény"),
    BANK_CATEGORY("Tranzakció kategória"),
    BANK_REFERENCE("Banki azonosító"),
    TRANSACTION_TIME("Tranzakció időpontja"),
    BOOKING_DATE("Könyvelés dátuma"),
    AMOUNT("Összeg"),
    CURRENCY("Devizanem");

    private final String header;

    Column(String header) {
      this.header = header;
    }
  }

  private final DataFormatter formatter = new DataFormatter();

  @Override
  public Statement parse(byte[] content) {
    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
      List<Row> rows = StreamSupport.stream(workbook.getSheetAt(0).spliterator(), false).toList();
      Row header =
          rows.stream()
              .filter(this::isHeader)
              .findFirst()
              .orElseThrow(
                  () -> new InvalidTransactionFileException("The header line was not found"));
      Map<Column, Integer> positions = positionsIn(header);
      return new Statement(
          queryEndIn(rows),
          rows.stream()
              .filter(row -> row.getRowNum() > header.getRowNum())
              .filter(row -> !text(row, positions.get(Column.BANK_REFERENCE)).isEmpty())
              .map(row -> toTransaction(row, positions))
              .toList());
    } catch (InvalidTransactionFileException e) {
      throw e;
    } catch (IOException | RuntimeException e) {
      throw new InvalidTransactionFileException("The file is not a transaction file", e);
    }
  }

  private boolean isHeader(Row row) {
    return headersOf(row).keySet().containsAll(Arrays.stream(Column.values()).map(c -> c.header).toList());
  }

  private Map<Column, Integer> positionsIn(Row header) {
    Map<String, Integer> positions = headersOf(header);
    return Arrays.stream(Column.values())
        .collect(Collectors.toMap(Function.identity(), column -> positions.get(column.header)));
  }

  /** Header text to column position. A repeated text keeps its first position. */
  private Map<String, Integer> headersOf(Row row) {
    return StreamSupport.stream(row.spliterator(), false)
        .collect(Collectors.toMap(this::text, Cell::getColumnIndex, (first, second) -> first));
  }

  private LocalDate queryEndIn(List<Row> rows) {
    return rows.stream()
        .filter(row -> QUERY_END_LABEL.equals(text(row, 0)))
        .findFirst()
        .map(row -> text(row, 1))
        .map(LocalDate::parse)
        .orElseThrow(
            () -> new InvalidTransactionFileException("'%s' was not found".formatted(QUERY_END_LABEL)));
  }

  private Transaction toTransaction(Row row, Map<Column, Integer> positions) {
    Function<Column, String> value = column -> text(row, positions.get(column));
    Function<Column, Optional<String>> optional =
        column -> Optional.of(value.apply(column)).filter(Predicate.not(String::isEmpty));
    return new Transaction(
        value.apply(Column.BANK_REFERENCE),
        value.apply(Column.ACCOUNT_NUMBER),
        optional.apply(Column.COUNTERPARTY_ACCOUNT_NUMBER),
        value.apply(Column.COUNTERPARTY_NAME),
        value.apply(Column.TYPE),
        optional.apply(Column.MEMO),
        value.apply(Column.BANK_CATEGORY),
        LocalDateTime.parse(value.apply(Column.TRANSACTION_TIME), TRANSACTION_TIME),
        LocalDate.parse(value.apply(Column.BOOKING_DATE)),
        amount(row.getCell(positions.get(Column.AMOUNT))),
        value.apply(Column.CURRENCY));
  }

  private BigDecimal amount(Cell cell) {
    return Optional.ofNullable(cell)
        .filter(c -> c.getCellType() == CellType.NUMERIC)
        .map(c -> BigDecimal.valueOf(c.getNumericCellValue()))
        .orElseGet(() -> new BigDecimal(text(cell)));
  }

  private String text(Row row, int position) {
    return text(row.getCell(position));
  }

  private String text(Cell cell) {
    return formatter.formatCellValue(cell).strip();
  }
}
