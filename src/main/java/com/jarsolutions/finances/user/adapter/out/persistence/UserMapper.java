package com.jarsolutions.finances.user.adapter.out.persistence;

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
}
