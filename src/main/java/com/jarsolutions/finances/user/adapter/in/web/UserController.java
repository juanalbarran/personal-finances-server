package com.jarsolutions.finances.user.adapter.in.web;

import com.jarsolutions.finances.user.application.port.in.CreateUserUseCase;
import com.jarsolutions.finances.user.application.port.in.DeleteUserUseCase;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import com.jarsolutions.finances.user.application.port.in.GetUsersUseCase;
import com.jarsolutions.finances.user.application.port.in.UpdateUserUseCase;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserController {

  private final GetUsersUseCase getUsersUseCase;
  private final GetUserUseCase getUserUseCase;
  private final CreateUserUseCase createUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;

  UserController(
      GetUsersUseCase getUsersUseCase,
      GetUserUseCase getUserUseCase,
      CreateUserUseCase createUserUseCase,
      UpdateUserUseCase updateUserUseCase,
      DeleteUserUseCase deleteUserUseCase) {
    this.getUsersUseCase = getUsersUseCase;
    this.getUserUseCase = getUserUseCase;
    this.createUserUseCase = createUserUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.deleteUserUseCase = deleteUserUseCase;
  }

  @GetMapping("/users")
  List<UserResponse> list() {
    return getUsersUseCase.getUsers().stream().map(UserResponse::from).toList();
  }

  @GetMapping("/users/{username}")
  UserResponse get(@PathVariable String username) {
    return UserResponse.from(getUserUseCase.getUser(username));
  }

  /** The new user starts with the default categories and rules. */
  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  UserResponse create(@RequestBody UserRequest request) {
    return UserResponse.from(createUserUseCase.createUser(request.toNewUser()));
  }

  @PutMapping("/users/{username}")
  UserResponse update(@PathVariable String username, @RequestBody UserProfileRequest request) {
    return UserResponse.from(updateUserUseCase.updateUser(username, request.toProfile()));
  }

  @DeleteMapping("/users/{username}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable String username) {
    deleteUserUseCase.deleteUser(username);
  }
}
