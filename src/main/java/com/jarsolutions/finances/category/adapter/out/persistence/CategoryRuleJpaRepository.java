package com.jarsolutions.finances.category.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryRuleJpaRepository extends JpaRepository<CategoryRuleJpaEntity, Long> {

  List<CategoryRuleJpaEntity> findAllByUserIdOrderById(Long userId);
}
