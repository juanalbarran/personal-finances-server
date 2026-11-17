package com.jarsolutions.finances.user.adapter.out.persistence;

import com.jarsolutions.finances.common.PersistenceAdapter;
import com.jarsolutions.finances.user.application.domain.model.NewUser;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.domain.model.UserHasTransactionsException;
import com.jarsolutions.finances.user.application.domain.model.UserNotFoundException;
import com.jarsolutions.finances.user.application.port.out.CreateUserPort;
import com.jarsolutions.finances.user.application.port.out.DeleteUserPort;
import com.jarsolutions.finances.user.application.port.out.LoadUserByEmailPort;
import com.jarsolutions.finances.user.application.port.out.LoadUserPort;
import com.jarsolutions.finances.user.application.port.out.LoadUsersPort;
import com.jarsolutions.finances.user.application.port.out.UpdateUserPort;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;

@PersistenceAdapter
class UserPersistenceAdapter
    implements LoadUserPort,
        LoadUserByEmailPort,
        LoadUsersPort,
        CreateUserPort,
        UpdateUserPort,
        DeleteUserPort {

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

  @Override
  public Optional<User> loadUserByEmail(String email) {
    return userRepository.findByEmailIgnoreCase(email).map(userMapper::toDomain);
  }

  @Override
  public List<User> loadUsers() {
    return userRepository.findAll().stream().map(userMapper::toDomain).toList();
  }

  @Override
  public User createUser(NewUser newUser) {
    return userMapper.toDomain(userRepository.saveAndFlush(userMapper.toEntity(newUser)));
  }

  @Override
  public User updateUser(User user) {
    UserJpaEntity entity =
        userRepository
            .findById(user.id())
            .orElseThrow(() -> new UserNotFoundException(user.username()));
    entity.changeProfile(user.email(), user.name(), user.lastName());
    return userMapper.toDomain(userRepository.saveAndFlush(entity));
  }

  /** The database deletes the categories and the rules of the user, and refuses the transactions. */
  @Override
  public void deleteUser(User user) {
    try {
      userRepository.deleteById(user.id());
      userRepository.flush();
    } catch (DataIntegrityViolationException e) {
      throw new UserHasTransactionsException(user.username());
    }
  }
}
