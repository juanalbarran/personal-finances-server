package com.jarsolutions.finances.user.adapter.out.persistence;

import com.jarsolutions.finances.common.PersistenceAdapter;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.port.out.LoadUserPort;
import java.util.Optional;

@PersistenceAdapter
class UserPersistenceAdapter implements LoadUserPort {

  private final UserJpaRepository userRepository;
  private final UserMapper userMapper;

  UserPersistenceAdapter(UserJpaRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  @Override
  public Optional<User> loadUser(String username) {
    return userRepository.findByUsername(username).map(userMapper::toDomain);
  }
}
