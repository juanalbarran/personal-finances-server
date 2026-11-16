package com.jarsolutions.finances.user.application.port.in;

import com.jarsolutions.finances.user.application.domain.model.User;
import java.util.Optional;

@FunctionalInterface
public interface GetUserUseCase {

  Optional<User> getUser(String username);
}
