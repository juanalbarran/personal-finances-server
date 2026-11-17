package com.jarsolutions.finances.transaction.adapter.out.persistence;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static com.jarsolutions.finances.transaction.TransactionFileFixture.transfer;
import static org.assertj.core.api.Assertions.assertThat;

import com.jarsolutions.finances.transaction.application.domain.model.CategorizedTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@Import({TransactionPersistenceAdapter.class, TransactionMapper.class})
class TransactionPersistenceAdapterTest {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  private static final TransactionFile OCTOBER =
      new TransactionFile(
          new TransactionFileName(YearMonth.of(2026, 10)),
          "a".repeat(64),
          Instant.parse("2026-11-16T10:00:00Z"),
          2);
  private static final Transaction PURCHASE =
      purchase("202610311905002717", "2026-10-31T19:05:25", "-845");
  private static final Transaction SALARY =
      transfer("202610071347002652", "2026-10-07T13:47:00", "1046823");

  @Autowired private TransactionPersistenceAdapter adapter;
  @Autowired private EntityManager entityManager;

  // The user the changelog inserts, another one and a category of the first
  private long user;
  private long otherUser;
  private long groceries;

  @BeforeEach
  void users() {
    user = id("select id from user_account where username = 'test.user'");
    otherUser =
        id(
            """
            insert into user_account (username, email, name, last_name)
            values ('other.user', 'other.user@example.com', 'Other', 'User') returning id
            """);
    groceries =
        id("select id from category where name = 'Groceries' and user_account_id = " + user);
  }

  @Test
  void save_storesTheFileAndItsTransactionsForTheUser() {
    adapter.saveTransactionFile(
        user,
        OCTOBER,
        List.of(
            new CategorizedTransaction(PURCHASE, Optional.of(groceries)),
            new CategorizedTransaction(SALARY, Optional.empty())));
    flushAndClear();

    assertThat(adapter.loadTransactionFiles(user)).containsExactly(OCTOBER);
    assertThat(adapter.loadTransactionFiles(otherUser)).isEmpty();
    assertThat(adapter.loadTransactions(otherUser, Optional.empty())).isEmpty();
    assertThat(adapter.loadTransactions(user, Optional.empty()))
        .satisfiesExactly(
            salary -> {
              assertThat(salary.transaction().bankReference()).isEqualTo("202610071347002652");
              assertThat(salary.transaction().amount()).isEqualByComparingTo("1046823");
              assertThat(salary.transaction().counterpartyAccountNumber())
                  .contains("222222222222222222222222");
              assertThat(salary.transaction().memo()).isEmpty();
              assertThat(salary.categoryId()).isEmpty();
            },
            purchase -> {
              assertThat(purchase.transaction().bankReference()).isEqualTo("202610311905002717");
              assertThat(purchase.transaction().amount()).isEqualByComparingTo("-845");
              assertThat(purchase.transaction().counterpartyAccountNumber()).isEmpty();
              assertThat(purchase.transaction().memo()).contains("0000000000 CORNER SHOP -ÉRINTŐ");
              assertThat(purchase.categoryId()).contains(groceries);
            });
  }

  @Test
  void twoUsersCanHaveTheSameMonthAndTheSameBankReference() {
    List<CategorizedTransaction> transactions =
        List.of(new CategorizedTransaction(PURCHASE, Optional.empty()));

    adapter.saveTransactionFile(user, OCTOBER, transactions);
    adapter.saveTransactionFile(otherUser, OCTOBER, transactions);
    flushAndClear();

    assertThat(adapter.loadTransactions(user, Optional.empty())).hasSize(1);
    assertThat(adapter.loadTransactions(otherUser, Optional.empty())).hasSize(1);
  }

  @Test
  void loadTransactions_ofAMonth_returnsOnlyTheOnesThatHappenedInIt() {
    adapter.saveTransactionFile(
        user,
        OCTOBER,
        List.of(
            new CategorizedTransaction(PURCHASE, Optional.empty()),
            new CategorizedTransaction(
                purchase("november", "2026-11-01T00:00:00", "-1"), Optional.empty())));
    flushAndClear();

    assertThat(adapter.loadTransactions(user, Optional.of(YearMonth.of(2026, 10))))
        .extracting(stored -> stored.transaction().bankReference())
        .containsExactly("202610311905002717");
  }

  @Test
  void loadExistingBankReferences_returnsOnlyTheOnesTheUserHas() {
    adapter.saveTransactionFile(
        user, OCTOBER, List.of(new CategorizedTransaction(PURCHASE, Optional.empty())));
    entityManager.flush();

    assertThat(adapter.loadExistingBankReferences(user, List.of("202610311905002717", "unknown")))
        .containsExactly("202610311905002717");
    assertThat(adapter.loadExistingBankReferences(otherUser, List.of("202610311905002717")))
        .isEmpty();
    assertThat(adapter.loadExistingBankReferences(user, List.of())).isEmpty();
  }

  @Test
  void categories_areSetOnTheUncategorizedSpendsAndByHand() {
    adapter.saveTransactionFile(
        user,
        OCTOBER,
        List.of(
            new CategorizedTransaction(PURCHASE, Optional.empty()),
            new CategorizedTransaction(SALARY, Optional.empty())));
    flushAndClear();

    // The salary is not a spend
    List<StoredTransaction> spends = adapter.loadUncategorizedSpends(user);
    assertThat(spends)
        .extracting(stored -> stored.transaction().bankReference())
        .containsExactly(PURCHASE.bankReference());
    long purchase = spends.getFirst().id();

    adapter.updateTransactionCategories(Map.of(purchase, groceries));
    flushAndClear();
    assertThat(adapter.loadUncategorizedSpends(user)).isEmpty();

    assertThat(adapter.updateTransactionCategory(user, purchase, Optional.empty()))
        .get()
        .extracting(StoredTransaction::categoryId)
        .isEqualTo(Optional.empty());
    assertThat(adapter.updateTransactionCategory(otherUser, purchase, Optional.of(groceries)))
        .isEmpty();
  }

  @Test
  void deletingACategory_leavesItsTransactionsWithoutCategory() {
    adapter.saveTransactionFile(
        user, OCTOBER, List.of(new CategorizedTransaction(PURCHASE, Optional.of(groceries))));
    flushAndClear();

    entityManager.createNativeQuery("delete from category where id = " + groceries).executeUpdate();

    assertThat(adapter.loadTransactions(user, Optional.empty()))
        .extracting(StoredTransaction::categoryId)
        .containsExactly(Optional.empty());
  }

  @Test
  void loadTransactionFiles_whenNothingWasLoaded_returnsNothing() {
    assertThat(adapter.loadTransactionFiles(user)).isEmpty();
  }

  private long id(String sql) {
    return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }
}
