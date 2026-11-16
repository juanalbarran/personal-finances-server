package com.jarsolutions.finances.transaction.application.domain.service;

import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.port.in.GetTransactionFilesUseCase;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionFilesPort;
import java.util.Comparator;
import java.util.List;

@UseCase
class GetTransactionFilesService implements GetTransactionFilesUseCase {

  private final LoadTransactionFilesPort loadTransactionFilesPort;

  GetTransactionFilesService(LoadTransactionFilesPort loadTransactionFilesPort) {
    this.loadTransactionFilesPort = loadTransactionFilesPort;
  }

  @Override
  public List<TransactionFile> getTransactionFiles() {
    return loadTransactionFilesPort.loadTransactionFiles().stream()
        .sorted(
            Comparator.comparing((TransactionFile file) -> file.name().period()).reversed())
        .toList();
  }
}
