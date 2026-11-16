package com.jarsolutions.finances.transaction.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jarsolutions.finances.transaction.application.domain.model.InvalidTransactionFileException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFile;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileConflictException;
import com.jarsolutions.finances.transaction.application.domain.model.TransactionFileName;
import com.jarsolutions.finances.transaction.application.port.in.GetTransactionFilesUseCase;
import com.jarsolutions.finances.transaction.application.port.in.UploadTransactionFileUseCase;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionFileController.class)
class TransactionFileControllerTest {

  private static final TransactionFileName OCTOBER = new TransactionFileName(YearMonth.of(2026, 10));
  private static final TransactionFile LOADED =
      new TransactionFile(OCTOBER, "abc", Instant.parse("2026-11-16T10:00:00Z"), 83);
  private static final MockMultipartFile FILE =
      new MockMultipartFile("file", "export.xlsx", null, new byte[] {1, 2, 3});

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UploadTransactionFileUseCase uploadTransactionFileUseCase;
  @MockitoBean private GetTransactionFilesUseCase getTransactionFilesUseCase;

  @Test
  void upload_returnsTheLoadedFile() throws Exception {
    when(uploadTransactionFileUseCase.uploadTransactionFile(new byte[] {1, 2, 3}))
        .thenReturn(LOADED);

    mockMvc
        .perform(multipart("/transaction-files").file(FILE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName").value("2026.october.transactions.xlsx"))
        .andExpect(jsonPath("$.year").value(2026))
        .andExpect(jsonPath("$.month").value(10))
        .andExpect(jsonPath("$.checksum").value("abc"))
        .andExpect(jsonPath("$.loadedAt").value("2026-11-16T10:00:00Z"))
        .andExpect(jsonPath("$.transactionCount").value(83));
  }

  @Test
  void upload_whenTheFileIsNotATransactionFile_returns400() throws Exception {
    when(uploadTransactionFileUseCase.uploadTransactionFile(any()))
        .thenThrow(new InvalidTransactionFileException("The header line was not found"));

    mockMvc
        .perform(multipart("/transaction-files").file(FILE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("The header line was not found"));
  }

  @Test
  void upload_whenAnotherFileExistsForTheMonth_returns409() throws Exception {
    when(uploadTransactionFileUseCase.uploadTransactionFile(any()))
        .thenThrow(new TransactionFileConflictException(OCTOBER));

    mockMvc.perform(multipart("/transaction-files").file(FILE)).andExpect(status().isConflict());
  }

  @Test
  void list_returnsTheLoadedFiles() throws Exception {
    when(getTransactionFilesUseCase.getTransactionFiles()).thenReturn(List.of(LOADED));

    mockMvc
        .perform(get("/transaction-files"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].fileName").value("2026.october.transactions.xlsx"))
        .andExpect(jsonPath("$[0].transactionCount").value(83));
  }
}
