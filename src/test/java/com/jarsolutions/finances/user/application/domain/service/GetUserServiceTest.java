package com.jarsolutions.finances.user.application.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jarsolutions.finances.user.application.domain.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GetUserServiceTest {

  private static final User USER =
      new User(1L, "test.user", "test.user@example.com", "Test", "User");

  private final GetUserService service =
      new GetUserService(
          username -> Optional.of(USER).filter(user -> user.username().equals(username)));

  @Test
  void getUser_whenTheUserExists_returnsIt() {
    assertThat(service.getUser("test.user")).contains(USER);
  }

  @Test
  void getUser_whenTheUserDoesNotExist_returnsEmpty() {
    assertThat(service.getUser("nobody")).isEmpty();
  }
}
