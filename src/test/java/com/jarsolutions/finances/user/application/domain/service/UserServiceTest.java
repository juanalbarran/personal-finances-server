package com.jarsolutions.finances.user.application.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jarsolutions.finances.user.application.domain.exception.InvalidUserException;
import com.jarsolutions.finances.user.application.domain.exception.UserAlreadyExistsException;
import com.jarsolutions.finances.user.application.domain.exception.UserNotFoundException;
import com.jarsolutions.finances.user.application.domain.model.NewUser;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.domain.model.UserCreated;
import com.jarsolutions.finances.user.application.domain.model.UserProfile;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserServiceTest {

  private static final UserProfile ANA = new UserProfile("ana@example.com", "Ana", "Perez");

  // The database, in memory
  private final List<User> users = new ArrayList<>();
  private final List<UserCreated> published = new ArrayList<>();

  private final UserService service =
      new UserService(
          username -> users.stream().filter(user -> user.username().equals(username)).findFirst(),
          email -> users.stream().filter(user -> user.email().equalsIgnoreCase(email)).findFirst(),
          () -> List.copyOf(users),
          newUser -> {
            UserProfile profile = newUser.profile();
            User user =
                new User(
                    users.size() + 1,
                    newUser.username(),
                    profile.email(),
                    profile.name(),
                    profile.lastName());
            users.add(user);
            return user;
          },
          user -> {
            users.replaceAll(old -> old.id() == user.id() ? user : old);
            return user;
          },
          users::remove,
          published::add);

  @Test
  void create_savesTheUserAndTellsItWasCreated() {
    User ana = service.createUser(new NewUser("ana.perez", ANA));

    assertThat(service.getUser("ana.perez")).isEqualTo(ana);
    assertThat(published).containsExactly(new UserCreated(ana));
  }

  @Test
  void create_whenTheUsernameOrTheEmailIsTaken_refuses() {
    service.createUser(new NewUser("ana.perez", ANA));

    assertThatThrownBy(() -> service.createUser(new NewUser("ana.perez", ANA)))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("username");
    assertThatThrownBy(
            () ->
                service.createUser(
                    new NewUser("another.ana", new UserProfile("ANA@example.com", "Ana", "P"))))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("email");
    assertThat(published).hasSize(1);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "..", "../ana", "ana/perez", ".ana", "ana perez"})
  void aUsernameThatIsNotSafeAsADirectoryNameIsRefused(String username) {
    assertThatThrownBy(() -> new NewUser(username, ANA)).isInstanceOf(InvalidUserException.class);
  }

  @Test
  void aProfileNeedsAnEmailAddressAndTheNames() {
    assertThatThrownBy(() -> new UserProfile("ana", "Ana", "Perez"))
        .isInstanceOf(InvalidUserException.class);
    assertThatThrownBy(() -> new UserProfile("ana@example.com", " ", "Perez"))
        .isInstanceOf(InvalidUserException.class);
    assertThatThrownBy(() -> new UserProfile("ana@example.com", "Ana", null))
        .isInstanceOf(InvalidUserException.class);
  }

  @Test
  void update_changesTheProfileAndKeepsTheUsername() {
    service.createUser(new NewUser("ana.perez", ANA));
    service.createUser(new NewUser("juan", new UserProfile("juan@example.com", "Juan", "J")));

    User updated =
        service.updateUser("ana.perez", new UserProfile("ana@example.com", "Ana Maria", "Perez"));

    assertThat(updated.username()).isEqualTo("ana.perez");
    assertThat(service.getUser("ana.perez").name()).isEqualTo("Ana Maria");
    assertThatThrownBy(
            () -> service.updateUser("ana.perez", new UserProfile("juan@example.com", "A", "P")))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void getUsers_ordersThemByUsername_andDeleteRemovesOne() {
    service.createUser(new NewUser("juan", new UserProfile("juan@example.com", "Juan", "J")));
    service.createUser(new NewUser("ana.perez", ANA));

    assertThat(service.getUsers()).extracting(User::username).containsExactly("ana.perez", "juan");

    service.deleteUser("juan");

    assertThat(service.getUsers()).extracting(User::username).containsExactly("ana.perez");
    assertThatThrownBy(() -> service.getUser("juan")).isInstanceOf(UserNotFoundException.class);
    assertThatThrownBy(() -> service.deleteUser("juan")).isInstanceOf(UserNotFoundException.class);
  }
}
