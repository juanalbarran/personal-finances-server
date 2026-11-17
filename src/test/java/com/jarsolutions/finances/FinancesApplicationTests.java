package com.jarsolutions.finances;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static com.jarsolutions.finances.transaction.TransactionFileFixture.transfer;
import static com.jarsolutions.finances.transaction.TransactionFileFixture.xlsx;
import static org.assertj.core.api.Assertions.assertThat;

import com.jarsolutions.finances.category.application.domain.model.DefaultCategories;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
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

/** The whole application. The tests share the database, so they go in order. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinancesApplicationTests {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  /**
   * The files directory before the application starts: the September file of test.user, the user
   * the changelog inserts, and a file in a directory nobody owns.
   */
  static final Path FILES = filesDirectoryWithSeptember();

  @LocalServerPort private int port;

  @Autowired private JdbcTemplate jdbcTemplate;

  @DynamicPropertySource
  static void filesDirectory(DynamicPropertyRegistry registry) {
    registry.add("finances.transaction-files.directory", FILES::toString);
  }

  @Test
  @Order(1)
  void loadsTheFilesOfTheUserDirectoriesAtStartupAndTheUploadedOnes() throws IOException {
    // Startup: only the September transaction of the September file, and nothing of nobody
    assertThat(bankReferences("test.user")).containsExactly("september-1");
    assertThat(jdbcTemplate.queryForObject("select count(*) from transaction_file", Long.class))
        .isEqualTo(1);

    byte[] october =
        xlsx(
            LocalDate.of(2026, 10, 31),
            List.of(
                purchase("september-2", "2026-09-30T18:57:28", "-1200"),
                purchase("october-1", "2026-10-31T19:05:25", "-845"),
                transfer("october-2", "2026-10-07T13:47:00", "1046823")));

    assertThat(upload("test.user", october))
        .contains("\"fileName\":\"2026.october.transactions.xlsx\"")
        .contains("\"transactionCount\":2");
    assertThat(bankReferences("test.user"))
        .containsExactly("october-1", "october-2", "september-1");
    assertThat(Files.readAllBytes(FILES.resolve("test.user/2026.october.transactions.xlsx")))
        .isEqualTo(october);

    // The same file again changes nothing
    assertThat(upload("test.user", october)).contains("\"transactionCount\":2");
    assertThat(bankReferences("test.user")).hasSize(3);

    assertThat(get("/users/test.user/transaction-files"))
        .containsSubsequence(
            "\"fileName\":\"2026.october.transactions.xlsx\"",
            "\"fileName\":\"2026.september.transactions.xlsx\"");
    assertThat(status("GET", "/users/nobody/transaction-files", null)).isEqualTo(404);
  }

  @Test
  @Order(2)
  void theSpendsOfALoadedFileGetTheCategoryOfTheRules() {
    // The purchases of the fixture are groceries for the bank, the transfer is income
    assertThat(categoryNames("test.user"))
        .containsExactly(
            Map.entry("october-1", "Groceries"),
            Map.entry("september-1", "Groceries"));
    assertThat(get("/users/test.user/transactions?period=2026-10"))
        .contains("\"bankReference\":\"october-1\"", "\"bankReference\":\"october-2\"")
        .doesNotContain("september-1");
  }

  @Test
  @Order(3)
  void aNewUserStartsWithTheDefaultCategoriesAndItsOwnTransactions() throws IOException {
    assertThat(
            status(
                "POST",
                "/users",
                """
                {"username": "ana.perez", "email": "ana@example.com", "name": "Ana",
                 "lastName": "Perez"}
                """))
        .isEqualTo(201);
    assertThat(get("/users")).contains("\"username\":\"ana.perez\"", "\"username\":\"test.user\"");
    assertThat(get("/users/ana.perez/categories"))
        .containsSubsequence(
            DefaultCategories.NAMES.stream().map("\"name\":\"%s\""::formatted).toList());
    assertThat(get("/users/ana.perez/category-rules")).contains("\"pattern\":\"Wolt\"");

    // The same month and bank references as test.user, with a delivery this time
    byte[] october =
        xlsx(
            LocalDate.of(2026, 10, 31),
            List.of(
                purchase("october-1", "2026-10-31T19:05:25", "-845"),
                purchase("october-3", "WOLT", "2026-10-30T20:00:00", "-4000")));
    assertThat(upload("ana.perez", october)).contains("\"transactionCount\":2");
    assertThat(Files.exists(FILES.resolve("ana.perez/2026.october.transactions.xlsx"))).isTrue();
    assertThat(categoryNames("ana.perez"))
        .containsExactly(
            Map.entry("october-1", "Groceries"), Map.entry("october-3", "Food Delivery"));
    assertThat(bankReferences("test.user")).hasSize(3);
  }

  @Test
  @Order(4)
  void categoriesAndRulesAreManagedAndTheTransactionsFollow() {
    long groceries = categoryId("ana.perez", "Groceries");
    long caffes = categoryId("ana.perez", "Caffes");
    long shop = transactionId("ana.perez", "october-1");

    // By hand to a category, and to none
    assertThat(put("/users/ana.perez/transactions/%d/category".formatted(shop), caffes))
        .isEqualTo(200);
    assertThat(categoryNames("ana.perez")).containsEntry("october-1", "Caffes");
    assertThat(put("/users/ana.perez/transactions/%d/category".formatted(shop), null))
        .isEqualTo(200);
    assertThat(categoryNames("ana.perez")).doesNotContainKey("october-1");
    // Not with the category of another user
    assertThat(
            put(
                "/users/ana.perez/transactions/%d/category".formatted(shop),
                categoryId("test.user", "Caffes")))
        .isEqualTo(404);

    // A rule about the merchant wins over the one about the bank category
    assertThat(
            status(
                "POST",
                "/users/ana.perez/category-rules",
                "{\"categoryId\": %d, \"field\": \"COUNTERPARTY_NAME\", \"pattern\": \"corner\"}"
                    .formatted(caffes)))
        .isEqualTo(201);
    assertThat(post("/users/ana.perez/transactions/categorize")).contains("\"categorized\":1");
    assertThat(categoryNames("ana.perez")).containsEntry("october-1", "Caffes");

    // Renamed, and a name another category has is refused
    assertThat(
            status("PUT", "/users/ana.perez/categories/" + caffes, "{\"name\": \"Coffee shops\"}"))
        .isEqualTo(200);
    assertThat(status("PUT", "/users/ana.perez/categories/" + caffes, "{\"name\": \"groceries\"}"))
        .isEqualTo(409);
    assertThat(status("POST", "/users/ana.perez/categories", "{\"name\": \"Gifts\"}"))
        .isEqualTo(201);
    assertThat(status("POST", "/users/ana.perez/categories", "{\"name\": \"Gifts\"}"))
        .isEqualTo(409);

    // Deleted with its rules, its transactions are left without category
    assertThat(status("DELETE", "/users/ana.perez/categories/" + caffes, null)).isEqualTo(204);
    assertThat(get("/users/ana.perez/categories")).doesNotContain("Coffee shops");
    assertThat(get("/users/ana.perez/category-rules")).doesNotContain("corner");
    assertThat(categoryNames("ana.perez")).doesNotContainKey("october-1");
    assertThat(post("/users/ana.perez/transactions/categorize")).contains("\"categorized\":1");
    assertThat(categoryNames("ana.perez")).containsEntry("october-1", "Groceries");
    assertThat(groceries).isNotEqualTo(categoryId("test.user", "Groceries"));
  }

  @Test
  @Order(5)
  void aUserIsUpdatedAndDeletedOnlyWhileItHasNoTransactions() {
    assertThat(
            status(
                "PUT",
                "/users/ana.perez",
                "{\"email\": \"ana@example.com\", \"name\": \"Ana Maria\", \"lastName\": \"P\"}"))
        .isEqualTo(200);
    assertThat(get("/users/ana.perez")).contains("\"name\":\"Ana Maria\"");
    assertThat(
            status(
                "PUT",
                "/users/ana.perez",
                "{\"email\": \"test.user@example.com\", \"name\": \"Ana\", \"lastName\": \"P\"}"))
        .isEqualTo(409);

    assertThat(status("DELETE", "/users/ana.perez", null)).isEqualTo(409);
    assertThat(get("/users/ana.perez")).contains("ana@example.com");

    assertThat(
            status(
                "POST",
                "/users",
                "{\"username\": \"new.user\", \"email\": \"new@example.com\", \"name\": \"New\","
                    + " \"lastName\": \"User\"}"))
        .isEqualTo(201);
    assertThat(status("DELETE", "/users/new.user", null)).isEqualTo(204);
    assertThat(status("GET", "/users/new.user", null)).isEqualTo(404);
    assertThat(
            jdbcTemplate.queryForObject(
                """
                select count(*) from category c
                where c.user_account_id not in (select id from user_account)
                """,
                Long.class))
        .isZero();
  }

  private String upload(String username, byte[] content) {
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
        .uri("/users/{username}/transaction-files", username)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
        .retrieve()
        .body(String.class);
  }

  private String get(String uri) {
    return client().get().uri(uri).retrieve().body(String.class);
  }

  private String post(String uri) {
    return client().post().uri(uri).retrieve().body(String.class);
  }

  private int put(String uri, Long categoryId) {
    return status("PUT", uri, "{\"categoryId\": %s}".formatted(categoryId));
  }

  /** The status of the response, whatever it is. */
  private int status(String method, String uri, String json) {
    RestClient.RequestBodySpec request =
        client().method(org.springframework.http.HttpMethod.valueOf(method)).uri(uri);
    if (json != null) {
      request.contentType(MediaType.APPLICATION_JSON).body(json);
    }
    return request
        .retrieve()
        .onStatus(HttpStatusCode::isError, (req, response) -> {})
        .toBodilessEntity()
        .getStatusCode()
        .value();
  }

  private RestClient client() {
    return RestClient.create("http://localhost:" + port);
  }

  private List<String> bankReferences(String username) {
    return jdbcTemplate.queryForList(
        """
        select t.bank_reference from account_transaction t
        join user_account u on u.id = t.user_account_id
        where u.username = ? order by t.bank_reference
        """,
        String.class,
        username);
  }

  /** The category of each transaction of the user that has one, by bank reference. */
  private Map<String, String> categoryNames(String username) {
    return jdbcTemplate
        .queryForList(
            """
            select t.bank_reference, c.name from account_transaction t
            join user_account u on u.id = t.user_account_id
            join category c on c.id = t.category_id
            where u.username = ? order by t.bank_reference
            """,
            username)
        .stream()
        .collect(
            java.util.stream.Collectors.toMap(
                row -> (String) row.get("bank_reference"),
                row -> (String) row.get("name"),
                (first, second) -> first,
                java.util.LinkedHashMap::new));
  }

  private long categoryId(String username, String name) {
    return jdbcTemplate.queryForObject(
        """
        select c.id from category c join user_account u on u.id = c.user_account_id
        where u.username = ? and c.name = ?
        """,
        Long.class,
        username,
        name);
  }

  private long transactionId(String username, String bankReference) {
    return jdbcTemplate.queryForObject(
        """
        select t.id from account_transaction t join user_account u on u.id = t.user_account_id
        where u.username = ? and t.bank_reference = ?
        """,
        Long.class,
        username,
        bankReference);
  }

  private static Path filesDirectoryWithSeptember() {
    try {
      Path directory = Files.createTempDirectory("finances-files");
      byte[] september =
          xlsx(
              LocalDate.of(2026, 9, 30),
              List.of(
                  purchase("september-1", "2026-09-15T12:00:00", "-500"),
                  purchase("august-1", "2026-08-31T23:00:00", "-700")));
      Files.write(
          Files.createDirectory(directory.resolve("test.user"))
              .resolve("2026.september.transactions.xlsx"),
          september);
      Files.write(
          Files.createDirectory(directory.resolve("nobody"))
              .resolve("2026.september.transactions.xlsx"),
          september);
      return directory;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
