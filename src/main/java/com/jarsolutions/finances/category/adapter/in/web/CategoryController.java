package com.jarsolutions.finances.category.adapter.in.web;

import com.jarsolutions.finances.category.application.port.in.CreateCategoryUseCase;
import com.jarsolutions.finances.category.application.port.in.DeleteCategoryUseCase;
import com.jarsolutions.finances.category.application.port.in.GetCategoriesUseCase;
import com.jarsolutions.finances.category.application.port.in.RenameCategoryUseCase;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CategoryController {

  private final GetCategoriesUseCase getCategoriesUseCase;
  private final CreateCategoryUseCase createCategoryUseCase;
  private final RenameCategoryUseCase renameCategoryUseCase;
  private final DeleteCategoryUseCase deleteCategoryUseCase;

  CategoryController(
      GetCategoriesUseCase getCategoriesUseCase,
      CreateCategoryUseCase createCategoryUseCase,
      RenameCategoryUseCase renameCategoryUseCase,
      DeleteCategoryUseCase deleteCategoryUseCase) {
    this.getCategoriesUseCase = getCategoriesUseCase;
    this.createCategoryUseCase = createCategoryUseCase;
    this.renameCategoryUseCase = renameCategoryUseCase;
    this.deleteCategoryUseCase = deleteCategoryUseCase;
  }

  @GetMapping("/users/{username}/categories")
  List<CategoryResponse> list(@PathVariable String username) {
    return getCategoriesUseCase.getCategories(username).stream()
        .map(CategoryResponse::from)
        .toList();
  }

  @PostMapping("/users/{username}/categories")
  @ResponseStatus(HttpStatus.CREATED)
  CategoryResponse create(@PathVariable String username, @RequestBody CategoryRequest request) {
    return CategoryResponse.from(createCategoryUseCase.createCategory(username, request.name()));
  }

  @PutMapping("/users/{username}/categories/{id}")
  CategoryResponse rename(
      @PathVariable String username, @PathVariable long id, @RequestBody CategoryRequest request) {
    return CategoryResponse.from(
        renameCategoryUseCase.renameCategory(username, id, request.name()));
  }

  /** The rules of the category go with it, its transactions are left without category. */
  @DeleteMapping("/users/{username}/categories/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable String username, @PathVariable long id) {
    deleteCategoryUseCase.deleteCategory(username, id);
  }
}
