package com.jarsolutions.finances.user.application.port.out;

import com.jarsolutions.finances.user.application.domain.model.User;
import java.util.Optional;

@FunctionalInterface
public interface LoadUserPort {

  Optional<User> loadUser(String username);
}
