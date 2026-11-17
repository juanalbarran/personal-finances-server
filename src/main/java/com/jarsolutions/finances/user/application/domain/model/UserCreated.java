package com.jarsolutions.finances.user.application.domain.model;

/** Tells the rest of the application a user was created, in the transaction that created it. */
public record UserCreated(User user) {}
