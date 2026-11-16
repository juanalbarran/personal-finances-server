package com.jarsolutions.finances.user.adapter.in.web;

import com.jarsolutions.finances.user.application.domain.model.User;

record UserResponse(String username, String email, String name, String lastName) {

  static UserResponse from(User user) {
    return new UserResponse(user.username(), user.email(), user.name(), user.lastName());
  }
}
