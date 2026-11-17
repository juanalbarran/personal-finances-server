package com.jarsolutions.finances.category.adapter.in.event;

import com.jarsolutions.finances.category.application.port.in.CreateDefaultCategoriesUseCase;
import com.jarsolutions.finances.user.application.domain.model.UserCreated;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Gives every new user the default categories, in the transaction that creates the user. */
@Component
class UserCreatedListener {

  private final CreateDefaultCategoriesUseCase createDefaultCategoriesUseCase;

  UserCreatedListener(CreateDefaultCategoriesUseCase createDefaultCategoriesUseCase) {
    this.createDefaultCategoriesUseCase = createDefaultCategoriesUseCase;
  }

  @EventListener
  void on(UserCreated userCreated) {
    createDefaultCategoriesUseCase.createDefaultCategories(userCreated.user().id());
  }
}
