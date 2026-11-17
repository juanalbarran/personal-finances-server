package com.jarsolutions.finances.user.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

  Optional<UserJpaEntity> findByUsername(String username);

  Optional<UserJpaEntity> findByEmailIgnoreCase(String email);
}
