package com.jarsolutions.finances.user.application.domain.service;

import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import com.jarsolutions.finances.user.application.port.out.LoadUserPort;
import java.util.Optional;

@UseCase
class GetUserService implements GetUserUseCase {

  private final LoadUserPort loadUserPort;

  GetUserService(LoadUserPort loadUserPort) {
    this.loadUserPort = loadUserPort;
  }

  @Override
  public Optional<User> getUser(String username) {
    return loadUserPort.loadUser(username);
  }
}
