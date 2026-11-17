package com.jarsolutions.finances.category.adapter.in.web;

import com.jarsolutions.finances.category.application.domain.model.CategoryAlreadyExistsException;
import com.jarsolutions.finances.category.application.domain.model.CategoryNotFoundException;
import com.jarsolutions.finances.category.application.domain.model.CategoryRuleAlreadyExistsException;
import com.jarsolutions.finances.category.application.domain.model.CategoryRuleNotFoundException;
import com.jarsolutions.finances.category.application.domain.model.InvalidCategoryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** For every controller, because the transactions endpoints look the categories up too. */
@RestControllerAdvice
class CategoryExceptionHandler {

  @ExceptionHandler({CategoryNotFoundException.class, CategoryRuleNotFoundException.class})
  ProblemDetail notFound(RuntimeException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(InvalidCategoryException.class)
  ProblemDetail invalid(InvalidCategoryException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler({
    CategoryAlreadyExistsException.class,
    CategoryRuleAlreadyExistsException.class
  })
  ProblemDetail conflict(RuntimeException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }
}
