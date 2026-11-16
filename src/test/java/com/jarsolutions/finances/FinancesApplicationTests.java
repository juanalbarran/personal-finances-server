package com.jarsolutions.finances;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static com.jarsolutions.finances.transaction.TransactionFileFixture.transfer;
import static com.jarsolutions.finances.transaction.TransactionFileFixture.xlsx;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class FinancesApplicationTests {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  /** The files directory, with the September file in it before the application starts. */
  static final Path FILES = filesDirectoryWithSeptember();

  @LocalServerPort private int port;

  @Autowired private JdbcTemplate jdbcTemplate;

  @DynamicPropertySource
  static void filesDirectory(DynamicPropertyRegistry registry) {
    registry.add("finances.transaction-files.directory", FILES::toString);
  }

  @Test
  void loadsTheFileOfTheDirectoryAtStartupAndTheUploadedOne() throws IOException {
    // Startup: only the September transaction of the September file
    assertThat(bankReferences()).containsExactly("september-1");

    byte[] october =
        xlsx(
            LocalDate.of(2026, 10, 31),
            List.of(
                purchase("september-2", "2026-09-30T18:57:28", "-1200"),
                purchase("october-1", "2026-10-31T19:05:25", "-845"),
                transfer("october-2", "2026-10-07T13:47:00", "1046823")));

    assertThat(upload(october))
        .contains("\"fileName\":\"2026.october.transactions.xlsx\"")
        .contains("\"transactionCount\":2");
    assertThat(bankReferences()).containsExactly("october-1", "october-2", "september-1");
    assertThat(Files.readAllBytes(FILES.resolve("2026.october.transactions.xlsx")))
        .isEqualTo(october);

    // The same file again changes nothing
    assertThat(upload(october)).contains("\"transactionCount\":2");
    assertThat(bankReferences()).hasSize(3);

    assertThat(client().get().uri("/transaction-files").retrieve().body(String.class))
        .containsSubsequence(
            "\"fileName\":\"2026.october.transactions.xlsx\"",
            "\"fileName\":\"2026.september.transactions.xlsx\"");
  }

  private String upload(byte[] content) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new ByteArrayResource(content) {
          @Override
          public String getFilename() {
            return "export.xlsx";
          }
        });
    return client()
        .post()
        .uri("/transaction-files")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
        .retrieve()
        .body(String.class);
  }

  private RestClient client() {
    return RestClient.create("http://localhost:" + port);
  }

  private List<String> bankReferences() {
    return jdbcTemplate.queryForList(
        "select bank_reference from account_transaction order by bank_reference", String.class);
  }

  private static Path filesDirectoryWithSeptember() {
    try {
      Path directory = Files.createTempDirectory("finances-files");
      Files.write(
          directory.resolve("2026.september.transactions.xlsx"),
          xlsx(
              LocalDate.of(2026, 9, 30),
              List.of(
                  purchase("september-1", "2026-09-15T12:00:00", "-500"),
                  purchase("august-1", "2026-08-31T23:00:00", "-700"))));
      return directory;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
