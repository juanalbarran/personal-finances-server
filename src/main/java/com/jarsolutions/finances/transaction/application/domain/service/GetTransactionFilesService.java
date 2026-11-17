package com.jarsolutions.finances.transaction.application.domain.service;

import com.jarsolutions.finances.common.UseCase;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.port.in.GetTransactionFilesUseCase;
import com.jarsolutions.finances.transaction.application.port.out.LoadTransactionFilesPort;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import java.util.Comparator;
import java.util.List;

@UseCase
class GetTransactionFilesService implements GetTransactionFilesUseCase {

  private final GetUserUseCase getUserUseCase;
  private final LoadTransactionFilesPort loadTransactionFilesPort;

  GetTransactionFilesService(
      GetUserUseCase getUserUseCase, LoadTransactionFilesPort loadTransactionFilesPort) {
    this.getUserUseCase = getUserUseCase;
    this.loadTransactionFilesPort = loadTransactionFilesPort;
  }

  @Override
  public List<TransactionFile> getTransactionFiles(String username) {
    return loadTransactionFilesPort
        .loadTransactionFiles(getUserUseCase.getUser(username).id())
        .stream()
        .sorted(Comparator.comparing((TransactionFile file) -> file.name().period()).reversed())
        .toList();
  }
}
