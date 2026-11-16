package com.jarsolutions.finances.transaction.adapter.out.persistence;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static com.jarsolutions.finances.transaction.TransactionFileFixture.transfer;
import static org.assertj.core.api.Assertions.assertThat;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
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

  @Autowired private TransactionPersistenceAdapter adapter;
  @Autowired private TransactionJpaRepository transactionRepository;
  @Autowired private EntityManager entityManager;

  @Test
  void save_storesTheFileAndItsTransactions() {
    adapter.saveTransactionFile(
        OCTOBER,
        List.of(
            purchase("202610311905002717", "2026-10-31T19:05:25", "-845"),
            transfer("202610071347002652", "2026-10-07T13:47:00", "1046823")));
    entityManager.flush();
    entityManager.clear();

    assertThat(adapter.loadTransactionFiles()).containsExactly(OCTOBER);
    assertThat(transactionRepository.findAllByOrderByTransactionTimeAsc())
        .satisfiesExactly(
            salary -> {
              assertThat(salary.getBankReference()).isEqualTo("202610071347002652");
              assertThat(salary.getAmount()).isEqualByComparingTo("1046823");
              assertThat(salary.getCounterpartyAccountNumber())
                  .isEqualTo("222222222222222222222222");
              assertThat(salary.getMemo()).isNull();
            },
            purchase -> {
              assertThat(purchase.getBankReference()).isEqualTo("202610311905002717");
              assertThat(purchase.getAmount()).isEqualByComparingTo("-845");
              assertThat(purchase.getCounterpartyAccountNumber()).isNull();
              assertThat(purchase.getMemo()).isEqualTo("0000000000 CORNER SHOP -ÉRINTŐ");
            });
  }

  @Test
  void loadExistingBankReferences_returnsOnlyTheOnesInTheDatabase() {
    adapter.saveTransactionFile(
        OCTOBER, List.of(purchase("202610311905002717", "2026-10-31T19:05:25", "-845")));
    entityManager.flush();

    assertThat(adapter.loadExistingBankReferences(List.of("202610311905002717", "unknown")))
        .containsExactly("202610311905002717");
    assertThat(adapter.loadExistingBankReferences(List.of())).isEmpty();
  }

  @Test
  void loadTransactionFiles_whenNothingWasLoaded_returnsNothing() {
    assertThat(adapter.loadTransactionFiles()).isEmpty();
  }
}
