package com.jarsolutions.finances.user.application.port.out;

import com.jarsolutions.finances.user.application.domain.model.User;
import java.util.List;

@FunctionalInterface
public interface LoadUsersPort {

  List<User> loadUsers();
}
