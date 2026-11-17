package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileLocation;

@FunctionalInterface
public interface ReadStoredTransactionFilePort {

  byte[] readStoredTransactionFile(TransactionFileLocation location);
}
