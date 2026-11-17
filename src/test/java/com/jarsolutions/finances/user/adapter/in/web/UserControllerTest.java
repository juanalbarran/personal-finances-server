package com.jarsolutions.finances.user.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jarsolutions.finances.user.application.domain.model.NewUser;
import com.jarsolutions.finances.user.application.domain.model.User;
import com.jarsolutions.finances.user.application.domain.model.UserAlreadyExistsException;
import com.jarsolutions.finances.user.application.domain.model.UserHasTransactionsException;
import com.jarsolutions.finances.user.application.domain.model.UserNotFoundException;
import com.jarsolutions.finances.user.application.domain.model.UserProfile;
import com.jarsolutions.finances.user.application.port.in.CreateUserUseCase;
import com.jarsolutions.finances.user.application.port.in.DeleteUserUseCase;
import com.jarsolutions.finances.user.application.port.in.GetUserUseCase;
import com.jarsolutions.finances.user.application.port.in.GetUsersUseCase;
import com.jarsolutions.finances.user.application.port.in.UpdateUserUseCase;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

  private static final User ANA = new User(2, "ana.perez", "ana@example.com", "Ana", "Perez");
  private static final String ANA_JSON =
      """
      {"username": "ana.perez", "email": "ana@example.com", "name": "Ana", "lastName": "Perez"}
      """;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetUsersUseCase getUsersUseCase;
  @MockitoBean private GetUserUseCase getUserUseCase;
  @MockitoBean private CreateUserUseCase createUserUseCase;
  @MockitoBean private UpdateUserUseCase updateUserUseCase;
  @MockitoBean private DeleteUserUseCase deleteUserUseCase;

  @Test
  void list_returnsTheUsers() throws Exception {
    when(getUsersUseCase.getUsers()).thenReturn(List.of(ANA));

    mockMvc
        .perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].username").value("ana.perez"))
        .andExpect(jsonPath("$[0].id").doesNotExist());
  }

  @Test
  void get_returnsTheUser_or404() throws Exception {
    when(getUserUseCase.getUser("ana.perez")).thenReturn(ANA);
    when(getUserUseCase.getUser("nobody")).thenThrow(new UserNotFoundException("nobody"));

    mockMvc
        .perform(get("/users/ana.perez"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("ana@example.com"))
        .andExpect(jsonPath("$.name").value("Ana"))
        .andExpect(jsonPath("$.lastName").value("Perez"));
    mockMvc.perform(get("/users/nobody")).andExpect(status().isNotFound());
  }

  @Test
  void create_returns201WithTheUser() throws Exception {
    when(createUserUseCase.createUser(
            new NewUser("ana.perez", new UserProfile("ana@example.com", "Ana", "Perez"))))
        .thenReturn(ANA);

    mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(ANA_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("ana.perez"));
  }

  @Test
  void create_whenTheDataIsNotValid_returns400() throws Exception {
    mockMvc
        .perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"../ana\", \"email\": \"a@b.c\", \"name\": \"A\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_whenTheUserExists_returns409() throws Exception {
    when(createUserUseCase.createUser(any()))
        .thenThrow(new UserAlreadyExistsException("username", "ana.perez"));

    mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(ANA_JSON))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.detail").value("A user with the username ana.perez already exists"));
  }

  @Test
  void update_changesTheProfile() throws Exception {
    when(updateUserUseCase.updateUser(
            "ana.perez", new UserProfile("ana@example.com", "Ana", "Perez")))
        .thenReturn(ANA);

    mockMvc
        .perform(put("/users/ana.perez").contentType(MediaType.APPLICATION_JSON).content(ANA_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("ana.perez"));
  }

  @Test
  void delete_returns204_or409WhenTheUserHasTransactions() throws Exception {
    doThrow(new UserHasTransactionsException("juan")).when(deleteUserUseCase).deleteUser("juan");

    mockMvc.perform(delete("/users/ana.perez")).andExpect(status().isNoContent());
    verify(deleteUserUseCase).deleteUser("ana.perez");
    mockMvc.perform(delete("/users/juan")).andExpect(status().isConflict());
  }
}
