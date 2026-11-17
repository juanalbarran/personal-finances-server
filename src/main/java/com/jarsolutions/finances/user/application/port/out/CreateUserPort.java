package com.jarsolutions.finances.user.application.port.out;

import com.jarsolutions.finances.user.application.domain.model.NewUser;
import com.jarsolutions.finances.user.application.domain.model.User;

@FunctionalInterface
public interface CreateUserPort {

  User createUser(NewUser newUser);
}
