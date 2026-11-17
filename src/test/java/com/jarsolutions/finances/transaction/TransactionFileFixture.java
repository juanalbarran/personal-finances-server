package com.jarsolutions.finances.transaction;

import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Builds transaction files with made up data, laid out like the ones the bank exports. */
public final class TransactionFileFixture {

  private static final List<String> HEADERS =
      List.of(
          "Számlaszám",
          "Ellenoldali számlaszám",
          "Ellenoldali név",
          "Forgalom típusa",
          "Közlemény",
          "Tranzakció kategória",
          "Banki azonosító",
          "Tranzakció időpontja",
          "Könyvelés dátuma",
          "Összeg",
          "Devizanem");
  private static final DateTimeFormatter TRANSACTION_TIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private TransactionFileFixture() {}

  /** A card purchase in the corner shop, which the bank calls groceries (Bevásárlás). */
  public static Transaction purchase(String bankReference, String time, String amount) {
    return purchase(bankReference, "CORNER SHOP", time, amount);
  }

  /** A card purchase, which the bank calls groceries (Bevásárlás). */
  public static Transaction purchase(
      String bankReference, String counterpartyName, String time, String amount) {
    LocalDateTime transactionTime = LocalDateTime.parse(time);
    return new Transaction(
        bankReference,
        "1111111111111111",
        Optional.empty(),
        counterpartyName,
        "VÁSÁRLÁS KÁRTYÁVAL",
        Optional.of("0000000000 CORNER SHOP -ÉRINTŐ"),
        "Bevásárlás",
        transactionTime,
        transactionTime.toLocalDate().plusDays(1),
        new BigDecimal(amount),
        "HUF");
  }

  /** A transfer, which is the kind of transaction that has a counterparty account and no memo. */
  public static Transaction transfer(String bankReference, String time, String amount) {
    LocalDateTime transactionTime = LocalDateTime.parse(time);
    return new Transaction(
        bankReference,
        "1111111111111111",
        Optional.of("222222222222222222222222"),
        "SOME COMPANY",
        "NAPKÖZBENI ÁTUTALÁS",
        Optional.empty(),
        "Nem kategorizált",
        transactionTime,
        transactionTime.toLocalDate(),
        new BigDecimal(amount),
        "HUF");
  }

  public static byte[] xlsx(LocalDate queryEnd, List<Transaction> transactions) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Tranzakciók");
      sheet.createRow(0).createCell(0).setCellValue("Számlatörténet");
      line(sheet, 2, "Számlaszám", "[1111111111111111]");
      line(sheet, 3, "Lekérdezés időpontja", "2026-01-01 09:00:00");
      line(sheet, 4, "Lekérdezés kezdete", queryEnd.withDayOfMonth(1).toString());
      line(sheet, 5, "Lekérdezés vége", queryEnd.toString());
      Row header = sheet.createRow(14);
      IntStream.range(0, HEADERS.size())
          .forEach(i -> header.createCell(i).setCellValue(HEADERS.get(i)));
      IntStream.range(0, transactions.size())
          .forEach(i -> write(sheet.createRow(15 + i), transactions.get(i)));
      workbook.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void line(Sheet sheet, int rowNumber, String label, String value) {
    Row row = sheet.createRow(rowNumber);
    row.createCell(0).setCellValue(label);
    row.createCell(1).setCellValue(value);
  }

  private static void write(Row row, Transaction transaction) {
    row.createCell(0).setCellValue(transaction.accountNumber());
    row.createCell(1).setCellValue(transaction.counterpartyAccountNumber().orElse(""));
    row.createCell(2).setCellValue(transaction.counterpartyName());
    row.createCell(3).setCellValue(transaction.type());
    row.createCell(4).setCellValue(transaction.memo().orElse(""));
    row.createCell(5).setCellValue(transaction.bankCategory());
    row.createCell(6).setCellValue(transaction.bankReference());
    row.createCell(7).setCellValue(transaction.transactionTime().format(TRANSACTION_TIME));
    row.createCell(8).setCellValue(transaction.bookingDate().toString());
    row.createCell(9).setCellValue(transaction.amount().doubleValue());
    row.createCell(10).setCellValue(transaction.currency());
  }
}
