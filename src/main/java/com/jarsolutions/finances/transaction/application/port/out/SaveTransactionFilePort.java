package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.CategorizedTransaction;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import java.util.List;

@FunctionalInterface
public interface SaveTransactionFilePort {

  /** Saves the record of the loaded file together with its transactions, for the user. */
  void saveTransactionFile(
      long userId, TransactionFile file, List<CategorizedTransaction> transactions);
}
