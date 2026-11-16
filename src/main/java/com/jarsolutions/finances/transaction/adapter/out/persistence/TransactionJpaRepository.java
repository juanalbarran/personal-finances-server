package com.jarsolutions.finances.transaction.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

  @Query(
      "select t.bankReference from TransactionJpaEntity t where t.bankReference in :bankReferences")
  Set<String> findExistingBankReferences(
      @Param("bankReferences") Collection<String> bankReferences);

  List<TransactionJpaEntity> findAllByOrderByTransactionTimeAsc();
}
