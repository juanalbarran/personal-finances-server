package com.jarsolutions.finances.user.application.port.out;

import com.jarsolutions.finances.user.application.domain.model.User;

@FunctionalInterface
public interface UpdateUserPort {

  User updateUser(User user);
}
