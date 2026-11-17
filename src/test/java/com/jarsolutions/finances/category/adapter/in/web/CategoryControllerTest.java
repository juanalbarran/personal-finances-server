package com.jarsolutions.finances.category.adapter.in.web;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jarsolutions.finances.category.application.domain.model.Category;
import com.jarsolutions.finances.category.application.domain.model.CategoryAlreadyExistsException;
import com.jarsolutions.finances.category.application.domain.model.CategoryNotFoundException;
import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import com.jarsolutions.finances.category.application.domain.model.CategoryRuleAlreadyExistsException;
import com.jarsolutions.finances.category.application.domain.model.InvalidCategoryException;
import com.jarsolutions.finances.category.application.domain.model.RuleField;
import com.jarsolutions.finances.category.application.port.in.CreateCategoryRuleUseCase;
import com.jarsolutions.finances.category.application.port.in.CreateCategoryUseCase;
import com.jarsolutions.finances.category.application.port.in.DeleteCategoryRuleUseCase;
import com.jarsolutions.finances.category.application.port.in.DeleteCategoryUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategoriesUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategoryRulesUseCase;
import com.jarsolutions.finances.category.application.port.in.RenameCategoryUseCase;
import com.jarsolutions.finances.user.application.domain.model.UserNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({CategoryController.class, CategoryRuleController.class})
class CategoryControllerTest {

  private static final Category GROCERIES = new Category(3, 7, "Groceries");
  private static final CategoryRule WOLT =
      new CategoryRule(11, 7, 3, RuleField.COUNTERPARTY_NAME, "Wolt");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetCategoriesUseCase getCategoriesUseCase;
  @MockitoBean private CreateCategoryUseCase createCategoryUseCase;
  @MockitoBean private RenameCategoryUseCase renameCategoryUseCase;
  @MockitoBean private DeleteCategoryUseCase deleteCategoryUseCase;
  @MockitoBean private GetCategoryRulesUseCase getCategoryRulesUseCase;
  @MockitoBean private CreateCategoryRuleUseCase createCategoryRuleUseCase;
  @MockitoBean private DeleteCategoryRuleUseCase deleteCategoryRuleUseCase;

  @Test
  void list_returnsTheCategoriesOfTheUser_or404WhenTheUserDoesNotExist() throws Exception {
    when(getCategoriesUseCase.getCategories("juan")).thenReturn(List.of(GROCERIES));
    when(getCategoriesUseCase.getCategories("nobody"))
        .thenThrow(new UserNotFoundException("nobody"));

    mockMvc
        .perform(get("/users/juan/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(3))
        .andExpect(jsonPath("$[0].name").value("Groceries"));
    mockMvc.perform(get("/users/nobody/categories")).andExpect(status().isNotFound());
  }

  @Test
  void create_returns201_400WhenTheNameIsNotValid_409WhenItExists() throws Exception {
    when(createCategoryUseCase.createCategory("juan", "Groceries")).thenReturn(GROCERIES);
    when(createCategoryUseCase.createCategory("juan", ""))
        .thenThrow(new InvalidCategoryException("The name is required"));
    when(createCategoryUseCase.createCategory("juan", "Mom"))
        .thenThrow(new CategoryAlreadyExistsException("Mom"));

    mockMvc
        .perform(categoryRequest("Groceries"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(3));
    mockMvc.perform(categoryRequest("")).andExpect(status().isBadRequest());
    mockMvc
        .perform(categoryRequest("Mom"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("The category Mom already exists"));
  }

  @Test
  void rename_returnsTheCategory_or404() throws Exception {
    when(renameCategoryUseCase.renameCategory("juan", 3, "Food"))
        .thenReturn(new Category(3, 7, "Food"));
    when(renameCategoryUseCase.renameCategory("juan", 99, "Food"))
        .thenThrow(new CategoryNotFoundException(99));

    mockMvc
        .perform(
            put("/users/juan/categories/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Food\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Food"));
    mockMvc
        .perform(
            put("/users/juan/categories/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Food\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_returns204_or404() throws Exception {
    doThrow(new CategoryNotFoundException(99)).when(deleteCategoryUseCase).deleteCategory("juan", 99);

    mockMvc.perform(delete("/users/juan/categories/3")).andExpect(status().isNoContent());
    verify(deleteCategoryUseCase).deleteCategory("juan", 3);
    mockMvc.perform(delete("/users/juan/categories/99")).andExpect(status().isNotFound());
  }

  @Test
  void rules_areListedCreatedAndDeleted() throws Exception {
    when(getCategoryRulesUseCase.getCategoryRules("juan")).thenReturn(List.of(WOLT));
    when(createCategoryRuleUseCase.createCategoryRule(
            "juan", 3, RuleField.COUNTERPARTY_NAME, "Wolt"))
        .thenReturn(WOLT);

    mockMvc
        .perform(get("/users/juan/category-rules"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(11))
        .andExpect(jsonPath("$[0].categoryId").value(3))
        .andExpect(jsonPath("$[0].field").value("COUNTERPARTY_NAME"))
        .andExpect(jsonPath("$[0].pattern").value("Wolt"));
    mockMvc
        .perform(
            ruleRequest("{\"categoryId\": 3, \"field\": \"COUNTERPARTY_NAME\", \"pattern\": \"Wolt\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(11));
    mockMvc.perform(delete("/users/juan/category-rules/11")).andExpect(status().isNoContent());
    verify(deleteCategoryRuleUseCase).deleteCategoryRule("juan", 11);
  }

  @Test
  void createRule_returns400WhenItIsNotValid_409WhenItExists() throws Exception {
    when(createCategoryRuleUseCase.createCategoryRule("juan", 3, RuleField.TYPE, "FEE"))
        .thenThrow(new CategoryRuleAlreadyExistsException(RuleField.TYPE, "FEE"));

    mockMvc
        .perform(ruleRequest("{\"field\": \"TYPE\", \"pattern\": \"FEE\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("The categoryId is required"));
    mockMvc
        .perform(ruleRequest("{\"categoryId\": 3, \"field\": \"COLOR\", \"pattern\": \"FEE\"}"))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(ruleRequest("{\"categoryId\": 3, \"field\": \"TYPE\", \"pattern\": \"FEE\"}"))
        .andExpect(status().isConflict());
  }

  private static org.springframework.test.web.servlet.RequestBuilder categoryRequest(String name) {
    return post("/users/juan/categories")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\": \"%s\"}".formatted(name));
  }

  private static org.springframework.test.web.servlet.RequestBuilder ruleRequest(String json) {
    return post("/users/juan/category-rules").contentType(MediaType.APPLICATION_JSON).content(json);
  }
}
