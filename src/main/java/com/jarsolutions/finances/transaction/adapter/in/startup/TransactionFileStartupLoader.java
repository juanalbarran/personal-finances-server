package com.jarsolutions.finances.transaction.adapter.in.startup;

import com.jarsolutions.finances.transaction.application.port.in.LoadNewTransactionFilesUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** When the application starts, loads the new transaction files of the files directory. */
@Component
class TransactionFileStartupLoader implements ApplicationRunner {

  private final LoadNewTransactionFilesUseCase loadNewTransactionFilesUseCase;

  TransactionFileStartupLoader(LoadNewTransactionFilesUseCase loadNewTransactionFilesUseCase) {
    this.loadNewTransactionFilesUseCase = loadNewTransactionFilesUseCase;
  }

  @Override
  public void run(ApplicationArguments args) {
    loadNewTransactionFilesUseCase.loadNewTransactionFiles();
  }
}
