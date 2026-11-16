package com.jarsolutions.finances.transaction.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface TransactionFileJpaRepository extends JpaRepository<TransactionFileJpaEntity, Long> {}
