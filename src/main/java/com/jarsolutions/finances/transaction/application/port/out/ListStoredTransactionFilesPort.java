package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;
import java.util.List;

@FunctionalInterface
public interface ListStoredTransactionFilesPort {

  /** The transaction files of the files directory, of every user directory. */
  List<TransactionFileLocation> listStoredTransactionFiles();
}
