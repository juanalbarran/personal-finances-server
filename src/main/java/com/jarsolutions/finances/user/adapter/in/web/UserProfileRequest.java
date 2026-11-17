package com.jarsolutions.finances.user.adapter.in.web;

import com.jarsolutions.finances.user.application.domain.model.UserProfile;

record UserProfileRequest(String email, String name, String lastName) {

  UserProfile toProfile() {
    return new UserProfile(email, name, lastName);
  }
}
