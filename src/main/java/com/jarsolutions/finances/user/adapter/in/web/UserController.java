package com.jarsolutions.finances.user.adapter.in.web;

import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserController {

  private final GetUserUseCase getUserUseCase;

  UserController(GetUserUseCase getUserUseCase) {
    this.getUserUseCase = getUserUseCase;
  }

  @GetMapping("/user/{username}")
  ResponseEntity<UserResponse> getUser(@PathVariable String username) {
    return getUserUseCase
        .getUser(username)
        .map(UserResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
