package com.jarsolutions.finances.transaction.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface TransactionFileJpaRepository extends JpaRepository<TransactionFileJpaEntity, Long> {

  List<TransactionFileJpaEntity> findAllByUserId(Long userId);
}
