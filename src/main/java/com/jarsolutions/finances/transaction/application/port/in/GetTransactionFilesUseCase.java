package com.jarsolutions.finances.transaction.application.port.in;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import java.util.List;

@FunctionalInterface
public interface GetTransactionFilesUseCase {

  /** The loaded files of the user, the most recent month first. */
  List<TransactionFile> getTransactionFiles(String username);
}
