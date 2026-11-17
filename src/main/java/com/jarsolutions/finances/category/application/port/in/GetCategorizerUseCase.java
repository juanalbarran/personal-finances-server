package com.jarsolutions.finances.category.application.port.in;

import com.jarsolutions.finances.category.application.domain.model.Categorizer;

@FunctionalInterface
public interface GetCategorizerUseCase {

  /** What chooses the category of the spends of the user, with the rules the user has now. */
  Categorizer getCategorizer(long userId);
}
