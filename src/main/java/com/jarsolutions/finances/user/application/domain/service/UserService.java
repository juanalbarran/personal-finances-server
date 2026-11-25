package com.jarsolutions.finances.user.application.domain.service;

import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.user.application.domain.exception.UserAlreadyExistsException;
import com.jarsolutions.finances.user.application.domain.exception.UserNotFoundException;
import com.jarsolutions.finances.user.application.domain.model.NewUser;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.domain.model.UserCreated;
import com.jarsolutions.finances.user.application.domain.model.UserProfile;
import com.jarsolutions.finances.user.application.port.in.CreateUserUseCase;
import com.jarsolutions.finances.user.application.port.in.DeleteUserUseCase;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import com.jarsolutions.finances.user.application.port.in.GetUsersUseCase;
import com.jarsolutions.finances.user.application.port.in.UpdateUserUseCase;
import com.jarsolutions.finances.user.application.port.out.CreateUserPort;
import com.jarsolutions.finances.user.application.port.out.DeleteUserPort;
import com.jarsolutions.finances.user.application.port.out.LoadUserByEmailPort;
import com.jarsolutions.finances.user.application.port.out.LoadUserPort;
import com.jarsolutions.finances.user.application.port.out.LoadUsersPort;
import com.jarsolutions.finances.user.application.port.out.PublishUserCreatedPort;
import com.jarsolutions.finances.user.application.port.out.UpdateUserPort;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
class UserService
    implements GetUserUseCase,
        GetUsersUseCase,
        CreateUserUseCase,
        UpdateUserUseCase,
        DeleteUserUseCase {

  private final LoadUserPort loadUserPort;
  private final LoadUserByEmailPort loadUserByEmailPort;
  private final LoadUsersPort loadUsersPort;
  private final CreateUserPort createUserPort;
  private final UpdateUserPort updateUserPort;
  private final DeleteUserPort deleteUserPort;
  private final PublishUserCreatedPort publishUserCreatedPort;

  UserService(
      LoadUserPort loadUserPort,
      LoadUserByEmailPort loadUserByEmailPort,
      LoadUsersPort loadUsersPort,
      CreateUserPort createUserPort,
      UpdateUserPort updateUserPort,
      DeleteUserPort deleteUserPort,
      PublishUserCreatedPort publishUserCreatedPort) {
    this.loadUserPort = loadUserPort;
    this.loadUserByEmailPort = loadUserByEmailPort;
    this.loadUsersPort = loadUsersPort;
    this.createUserPort = createUserPort;
    this.updateUserPort = updateUserPort;
    this.deleteUserPort = deleteUserPort;
    this.publishUserCreatedPort = publishUserCreatedPort;
  }

  @Override
  @Transactional(readOnly = true)
  public User getUser(String username) {
    return loadUserPort.loadUser(username).orElseThrow(() -> new UserNotFoundException(username));
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> getUsers() {
    return loadUsersPort.loadUsers().stream().sorted(Comparator.comparing(User::username)).toList();
  }

  @Override
  public User createUser(NewUser newUser) {
    loadUserPort
        .loadUser(newUser.username())
        .ifPresent(
            existing -> {
              throw new UserAlreadyExistsException("username", newUser.username());
            });
    refuseTheEmailOfAnotherUser(newUser.profile().email(), other -> true);
    User user = createUserPort.createUser(newUser);
    publishUserCreatedPort.publish(new UserCreated(user));
    return user;
  }

  @Override
  public User updateUser(String username, UserProfile profile) {
    User user = getUser(username);
    refuseTheEmailOfAnotherUser(profile.email(), other -> other.id() != user.id());
    return updateUserPort.updateUser(user.withProfile(profile));
  }

  @Override
  public void deleteUser(String username) {
    deleteUserPort.deleteUser(getUser(username));
  }

  private void refuseTheEmailOfAnotherUser(String email, Predicate<User> isAnotherUser) {
    loadUserByEmailPort
        .loadUserByEmail(email)
        .filter(isAnotherUser)
        .ifPresent(
            other -> {
              throw new UserAlreadyExistsException("email", email);
            });
  }
}
