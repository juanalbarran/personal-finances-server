package com.jarsolutions.finances.transaction.application.domain.service;

import com.jarsolutions.finances.category.application.domain.model.Categorizer;
import com.jarsolutions.finances.category.application.domain.model.Spend;
import com.jarsolutions.finances.transaction.application.domain.model.Transaction;
import java.util.Optional;
import java.util.function.Function;

/** Chooses the category of the transactions with the rules of their user. */
final class TransactionCategorizer {

  private TransactionCategorizer() {}

  /** Only the spends get a category, the money that comes in has none. */
  static Function<Transaction, Optional<Long>> with(Categorizer categorizer) {
    return transaction ->
        Optional.of(transaction)
            .filter(Transaction::isSpend)
            .map(
                spend ->
                    new Spend(spend.counterpartyName(), spend.type(), spend.bankCategory()))
            .flatMap(categorizer::categoryOf);
  }
}
