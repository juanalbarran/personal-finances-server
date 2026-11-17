package com.jarsolutions.finances.transaction.adapter.in.web;

import com.jarsolutions.finances.transaction.application.domain.model.InvalidTransactionFileException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.port.in.GetTransactionFilesUseCase;
import com.jarsolutions.finances.transaction.application.port.in.UploadTransactionFileUseCase;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
class TransactionFileController {

  private final UploadTransactionFileUseCase uploadTransactionFileUseCase;
  private final GetTransactionFilesUseCase getTransactionFilesUseCase;

  TransactionFileController(
      UploadTransactionFileUseCase uploadTransactionFileUseCase,
      GetTransactionFilesUseCase getTransactionFilesUseCase) {
    this.uploadTransactionFileUseCase = uploadTransactionFileUseCase;
    this.getTransactionFilesUseCase = getTransactionFilesUseCase;
  }

  /** Loads a transaction file for the user. Sending the same file again returns the first load. */
  @PostMapping(
      path = "/users/{username}/transaction-files",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  TransactionFileResponse upload(
      @PathVariable String username, @RequestPart("file") MultipartFile file) throws IOException {
    return TransactionFileResponse.from(
        uploadTransactionFileUseCase.uploadTransactionFile(username, file.getBytes()));
  }

  @GetMapping("/users/{username}/transaction-files")
  List<TransactionFileResponse> list(@PathVariable String username) {
    return getTransactionFilesUseCase.getTransactionFiles(username).stream()
        .map(TransactionFileResponse::from)
        .toList();
  }

  @ExceptionHandler(InvalidTransactionFileException.class)
  ProblemDetail invalidFile(InvalidTransactionFileException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(TransactionFileConflictException.class)
  ProblemDetail conflict(TransactionFileConflictException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }
}
