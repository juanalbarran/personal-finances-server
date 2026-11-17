package com.jarsolutions.finances.category.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

  List<CategoryJpaEntity> findAllByUserIdOrderById(Long userId);

  Optional<CategoryJpaEntity> findByIdAndUserId(Long id, Long userId);
}
