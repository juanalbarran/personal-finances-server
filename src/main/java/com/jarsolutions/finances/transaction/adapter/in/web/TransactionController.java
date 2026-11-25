package com.jarsolutions.finances.transaction.adapter.in.web;

import com.jarsolutions.finances.transaction.application.domain.exception.TransactionNotFoundException;
import com.jarsolutions.finances.transaction.application.port.in.CategorizeTransactionsUseCase;
import com.jarsolutions.finances.transaction.application.port.in.GetTransactionsUseCase;
import com.jarsolutions.finances.transaction.application.port.in.SetTransactionCategoryUseCase;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TransactionController {

  private final GetTransactionsUseCase getTransactionsUseCase;
  private final SetTransactionCategoryUseCase setTransactionCategoryUseCase;
  private final CategorizeTransactionsUseCase categorizeTransactionsUseCase;

  TransactionController(
      GetTransactionsUseCase getTransactionsUseCase,
      SetTransactionCategoryUseCase setTransactionCategoryUseCase,
      CategorizeTransactionsUseCase categorizeTransactionsUseCase) {
    this.getTransactionsUseCase = getTransactionsUseCase;
    this.setTransactionCategoryUseCase = setTransactionCategoryUseCase;
    this.categorizeTransactionsUseCase = categorizeTransactionsUseCase;
  }

  /** The transactions of the user, of one month with {@code ?period=2026-10}. */
  @GetMapping("/users/{username}/transactions")
  List<TransactionResponse> list(
      @PathVariable String username, @RequestParam Optional<YearMonth> period) {
    return getTransactionsUseCase.getTransactions(username, period).stream()
        .map(TransactionResponse::from)
        .toList();
  }

  /** Puts the transaction in a category by hand, or in none with a null {@code categoryId}. */
  @PutMapping("/users/{username}/transactions/{id}/category")
  TransactionResponse setCategory(
      @PathVariable String username,
      @PathVariable long id,
      @RequestBody TransactionCategoryRequest request) {
    return TransactionResponse.from(
        setTransactionCategoryUseCase.setTransactionCategory(
            username, id, Optional.ofNullable(request.categoryId())));
  }

  /** Applies the rules of the user to the spends that have no category. */
  @PostMapping("/users/{username}/transactions/categorize")
  CategorizedTransactionsResponse categorize(@PathVariable String username) {
    return new CategorizedTransactionsResponse(
        categorizeTransactionsUseCase.categorizeTransactions(username));
  }

  @ExceptionHandler(TransactionNotFoundException.class)
  ProblemDetail notFound(TransactionNotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }
}
