package com.jarsolutions.finances.category.adapter.in.web;

import com.jarsolutions.finances.category.application.domain.exception.InvalidCategoryException;
import com.jarsolutions.finances.category.application.port.in.CreateCategoryRuleUseCase;
import com.jarsolutions.finances.category.application.port.in.DeleteCategoryRuleUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategoryRulesUseCase;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CategoryRuleController {

  private final GetCategoryRulesUseCase getCategoryRulesUseCase;
  private final CreateCategoryRuleUseCase createCategoryRuleUseCase;
  private final DeleteCategoryRuleUseCase deleteCategoryRuleUseCase;

  CategoryRuleController(
      GetCategoryRulesUseCase getCategoryRulesUseCase,
      CreateCategoryRuleUseCase createCategoryRuleUseCase,
      DeleteCategoryRuleUseCase deleteCategoryRuleUseCase) {
    this.getCategoryRulesUseCase = getCategoryRulesUseCase;
    this.createCategoryRuleUseCase = createCategoryRuleUseCase;
    this.deleteCategoryRuleUseCase = deleteCategoryRuleUseCase;
  }

  @GetMapping("/users/{username}/category-rules")
  List<CategoryRuleResponse> list(@PathVariable String username) {
    return getCategoryRulesUseCase.getCategoryRules(username).stream()
        .map(CategoryRuleResponse::from)
        .toList();
  }

  /** The rule is used for the files loaded from now on and when the transactions are categorized. */
  @PostMapping("/users/{username}/category-rules")
  @ResponseStatus(HttpStatus.CREATED)
  CategoryRuleResponse create(
      @PathVariable String username, @RequestBody CategoryRuleRequest request) {
    long categoryId =
        Optional.ofNullable(request.categoryId())
            .orElseThrow(() -> new InvalidCategoryException("The categoryId is required"));
    return CategoryRuleResponse.from(
        createCategoryRuleUseCase.createCategoryRule(
            username, categoryId, request.field(), request.pattern()));
  }

  @DeleteMapping("/users/{username}/category-rules/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable String username, @PathVariable long id) {
    deleteCategoryRuleUseCase.deleteCategoryRule(username, id);
  }
}
