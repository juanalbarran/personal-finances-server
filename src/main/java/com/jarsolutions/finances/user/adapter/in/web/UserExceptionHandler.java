package com.jarsolutions.finances.user.adapter.in.web;

import com.jarsolutions.finances.user.application.domain.model.InvalidUserException;
import com.jarsolutions.finances.user.application.domain.model.UserAlreadyExistsException;
import com.jarsolutions.finances.user.application.domain.model.UserHasTransactionsException;
import com.jarsolutions.finances.user.application.domain.model.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** For every controller, because every endpoint under {@code /users/{username}} looks the user up. */
@RestControllerAdvice
class UserExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  ProblemDetail notFound(UserNotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(InvalidUserException.class)
  ProblemDetail invalid(InvalidUserException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler({UserAlreadyExistsException.class, UserHasTransactionsException.class})
  ProblemDetail conflict(RuntimeException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }
}
