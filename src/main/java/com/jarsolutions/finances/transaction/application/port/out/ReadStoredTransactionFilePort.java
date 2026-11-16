package com.jarsolutions.finances.transaction.application.port.out;

import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;

@FunctionalInterface
public interface ReadStoredTransactionFilePort {

  byte[] readStoredTransactionFile(TransactionFileName name);
}
