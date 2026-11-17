package com.jarsolutions.finances.user.adapter.in.web;

import com.jarsolutions.finances.user.application.domain.model.NewUser;
import com.jarsolutions.finances.user.application.domain.model.UserProfile;

record UserRequest(String username, String email, String name, String lastName) {

  NewUser toNewUser() {
    return new NewUser(username, new UserProfile(email, name, lastName));
  }
}
