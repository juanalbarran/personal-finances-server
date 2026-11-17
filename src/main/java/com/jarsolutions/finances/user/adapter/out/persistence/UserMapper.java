package com.jarsolutions.finances.user.adapter.out.persistence;

import com.jarsolutions.finances.user.application.domain.model.NewUser;
import com.jarsolutions.finances.user.application.domain.model.User;
import org.springframework.stereotype.Component;

@Component
class UserMapper {

  User toDomain(UserJpaEntity entity) {
    return new User(
        entity.getId(),
        entity.getUsername(),
        entity.getEmail(),
        entity.getName(),
        entity.getLastName());
  }

  UserJpaEntity toEntity(NewUser newUser) {
    return new UserJpaEntity(
        newUser.username(),
        newUser.profile().email(),
        newUser.profile().name(),
        newUser.profile().lastName());
  }
}
