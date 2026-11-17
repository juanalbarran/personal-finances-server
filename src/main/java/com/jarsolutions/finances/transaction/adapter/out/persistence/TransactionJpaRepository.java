package com.jarsolutions.finances.transaction.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

  @Query(
      """
      select t.bankReference from TransactionJpaEntity t
      where t.userId = :userId and t.bankReference in :bankReferences
      """)
  Set<String> findExistingBankReferences(
      @Param("userId") Long userId, @Param("bankReferences") Collection<String> bankReferences);

  List<TransactionJpaEntity> findAllByUserIdOrderByTransactionTimeAscIdAsc(Long userId);

  /** The transactions from a moment on and before another. */
  @Query(
      """
      select t from TransactionJpaEntity t
      where t.userId = :userId and t.transactionTime >= :from and t.transactionTime < :until
      order by t.transactionTime, t.id
      """)
  List<TransactionJpaEntity> findAllOfThePeriod(
      @Param("userId") Long userId,
      @Param("from") LocalDateTime from,
      @Param("until") LocalDateTime until);

  @Query(
      """
      select t from TransactionJpaEntity t
      where t.userId = :userId and t.categoryId is null and t.amount < 0
      order by t.transactionTime, t.id
      """)
  List<TransactionJpaEntity> findAllUncategorizedSpends(@Param("userId") Long userId);

  Optional<TransactionJpaEntity> findByIdAndUserId(Long id, Long userId);
}
