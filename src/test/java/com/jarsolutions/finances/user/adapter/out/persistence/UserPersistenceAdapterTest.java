package com.jarsolutions.finances.user.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.jarsolutions.finances.user.application.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@Import({UserPersistenceAdapter.class, UserMapper.class})
class UserPersistenceAdapterTest {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  @Autowired private UserPersistenceAdapter adapter;

  @Test
  void loadUser_returnsTheFirstUserInsertedByLiquibase() {
    assertThat(adapter.loadUser("test.user"))
        .map(User::email)
        .contains("test.user@example.com");
  }

  @Test
  void loadUser_whenTheUserDoesNotExist_returnsEmpty() {
    assertThat(adapter.loadUser("nobody")).isEmpty();
  }
}
