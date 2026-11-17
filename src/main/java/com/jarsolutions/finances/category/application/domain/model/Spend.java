package com.jarsolutions.finances.category.application.domain.model;

/** What the rules look at to choose the category of a spend. */
public record Spend(String counterpartyName, String type, String bankCategory) {}
