package com.jarsolutions.finances.user.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetUserUseCase getUserUseCase;

  @Test
  void getUser_whenTheUserExists_returnsItsData() throws Exception {
    when(getUserUseCase.getUser("test.user"))
        .thenReturn(
            Optional.of(new User(1L, "test.user", "test.user@example.com", "Test", "User")));

    mockMvc
        .perform(get("/user/test.user"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("test.user"))
        .andExpect(jsonPath("$.email").value("test.user@example.com"))
        .andExpect(jsonPath("$.name").value("Test"))
        .andExpect(jsonPath("$.lastName").value("User"))
        .andExpect(jsonPath("$.id").doesNotExist());
  }

  @Test
  void getUser_whenTheUserDoesNotExist_returns404() throws Exception {
    when(getUserUseCase.getUser("nobody")).thenReturn(Optional.empty());

    mockMvc.perform(get("/user/nobody")).andExpect(status().isNotFound());
  }
}
