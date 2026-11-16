package com.jarsolutions.finances;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class FinancesApplicationTests {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  @LocalServerPort private int port;

  @Test
  void getUser_returnsTheFirstUserInsertedByLiquibase() {
    String body =
        RestClient.create("http://localhost:" + port)
            .get()
            .uri("/user/{username}", "test.user")
            .retrieve()
            .body(String.class);

    assertThat(body)
        .contains("\"username\":\"test.user\"")
        .contains("\"email\":\"test.user@example.com\"")
        .contains("\"name\":\"Test\"")
        .contains("\"lastName\":\"User\"");
  }
}
