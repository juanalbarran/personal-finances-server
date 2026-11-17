package com.jarsolutions.finances.user.adapter.out.event;

import com.jarsolutions.finances.user.application.domain.model.UserCreated;
import com.jarsolutions.finances.user.application.port.out.PublishUserCreatedPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Publishes the events of the users as Spring application events. */
@Component
class UserEventPublisher implements PublishUserCreatedPort {

  private final ApplicationEventPublisher applicationEventPublisher;

  UserEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void publish(UserCreated userCreated) {
    applicationEventPublisher.publishEvent(userCreated);
  }
}
