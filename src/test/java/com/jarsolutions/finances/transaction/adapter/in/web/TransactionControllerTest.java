package com.jarsolutions.finances.transaction.adapter.in.web;

import static com.jarsolutions.finances.transaction.TransactionFileFixture.purchase;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jarsolutions.finances.category.application.domain.exception.CategoryNotFoundException;
import com.jarsolutions.finances.transaction.application.domain.exception.TransactionNotFoundException;
import com.jarsolutions.finances.transaction.application.domain.model.StoredTransaction;
import com.jarsolutions.finances.transaction.application.port.in.CategorizeTransactionsUseCase;
import com.jarsolutions.finances.transaction.application.port.in.GetTransactionsUseCase;
import com.jarsolutions.finances.transaction.application.port.in.SetTransactionCategoryUseCase;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  private static final StoredTransaction SHOP =
      new StoredTransaction(5, purchase("shop", "2026-10-31T19:05:25", "-845"), Optional.of(3L));

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetTransactionsUseCase getTransactionsUseCase;
  @MockitoBean private SetTransactionCategoryUseCase setTransactionCategoryUseCase;
  @MockitoBean private CategorizeTransactionsUseCase categorizeTransactionsUseCase;

  @Test
  void list_returnsTheTransactionsOfTheUser() throws Exception {
    when(getTransactionsUseCase.getTransactions("juan", Optional.empty()))
        .thenReturn(List.of(SHOP));

    mockMvc
        .perform(get("/users/juan/transactions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(5))
        .andExpect(jsonPath("$[0].categoryId").value(3))
        .andExpect(jsonPath("$[0].bankReference").value("shop"))
        .andExpect(jsonPath("$[0].counterpartyName").value("CORNER SHOP"))
        .andExpect(jsonPath("$[0].counterpartyAccountNumber").doesNotExist())
        .andExpect(jsonPath("$[0].bankCategory").value("Bevásárlás"))
        .andExpect(jsonPath("$[0].transactionTime").value("2026-10-31T19:05:25"))
        .andExpect(jsonPath("$[0].bookingDate").value("2026-11-01"))
        .andExpect(jsonPath("$[0].amount").value(-845))
        .andExpect(jsonPath("$[0].currency").value("HUF"));
  }

  @Test
  void list_ofAMonth_takesThePeriod_and400WhenItIsNotAMonth() throws Exception {
    when(getTransactionsUseCase.getTransactions("juan", Optional.of(YearMonth.of(2026, 10))))
        .thenReturn(List.of(SHOP));

    mockMvc
        .perform(get("/users/juan/transactions").param("period", "2026-10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(5));
    mockMvc
        .perform(get("/users/juan/transactions").param("period", "october"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void setCategory_putsTheTransactionInTheCategoryOrInNone() throws Exception {
    when(setTransactionCategoryUseCase.setTransactionCategory("juan", 5, Optional.of(3L)))
        .thenReturn(SHOP);
    when(setTransactionCategoryUseCase.setTransactionCategory("juan", 5, Optional.empty()))
        .thenReturn(new StoredTransaction(5, SHOP.transaction(), Optional.empty()));

    mockMvc
        .perform(categoryRequest(5, "{\"categoryId\": 3}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categoryId").value(3));
    mockMvc
        .perform(categoryRequest(5, "{\"categoryId\": null}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categoryId").doesNotExist());
  }

  @Test
  void setCategory_whenTheTransactionOrTheCategoryDoesNotExist_returns404() throws Exception {
    when(setTransactionCategoryUseCase.setTransactionCategory("juan", 9, Optional.of(3L)))
        .thenThrow(new TransactionNotFoundException(9));
    when(setTransactionCategoryUseCase.setTransactionCategory("juan", 5, Optional.of(99L)))
        .thenThrow(new CategoryNotFoundException(99));

    mockMvc
        .perform(categoryRequest(9, "{\"categoryId\": 3}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("The transaction 9 does not exist"));
    mockMvc
        .perform(categoryRequest(5, "{\"categoryId\": 99}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("The category 99 does not exist"));
  }

  @Test
  void categorize_returnsHowManyTransactionsGotACategory() throws Exception {
    when(categorizeTransactionsUseCase.categorizeTransactions("juan")).thenReturn(12);

    mockMvc
        .perform(post("/users/juan/transactions/categorize"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categorized").value(12));
  }

  private static org.springframework.test.web.servlet.RequestBuilder categoryRequest(
      long transactionId, String json) {
    return put("/users/juan/transactions/%d/category".formatted(transactionId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json);
  }
}
