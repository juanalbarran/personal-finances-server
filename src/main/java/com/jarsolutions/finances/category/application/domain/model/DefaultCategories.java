package com.jarsolutions.finances.category.application.domain.model;

import java.util.List;

/**
 * The categories and the rules a new user starts with. The users that existed before the categories
 * got the same ones from the changelog 20261117-02-insert-default-categories.
 */
public final class DefaultCategories {

  /** A rule of a category that is known by its name, because it has no id yet. */
  public record Rule(String categoryName, RuleField field, String pattern) {}

  public static final List<String> NAMES =
      List.of(
          "Home and Furniture",
          "Groceries",
          "Flights",
          "Mom",
          "Eating out",
          "Caffes",
          "Pubs",
          "Food Delivery",
          "Revolut",
          "Transport",
          "Health and Beauty",
          "Wellness",
          "Subscriptions",
          "Other shopping",
          "Bank fees");

  public static final List<Rule> RULES =
      List.of(
          new Rule("Groceries", RuleField.BANK_CATEGORY, "Bevásárlás"),
          new Rule("Eating out", RuleField.BANK_CATEGORY, "Vendéglátás"),
          new Rule("Transport", RuleField.BANK_CATEGORY, "Közlekedés"),
          new Rule("Health and Beauty", RuleField.BANK_CATEGORY, "Egészség, szépség"),
          new Rule("Home and Furniture", RuleField.BANK_CATEGORY, "Otthon"),
          new Rule("Other shopping", RuleField.BANK_CATEGORY, "Ruházat"),
          new Rule("Bank fees", RuleField.TYPE, "BANKKÁRTYÁVAL KAPCS. DÍJ"),
          new Rule("Bank fees", RuleField.TYPE, "ELEM: E-UTALÁS+ 400EFT"),
          new Rule("Bank fees", RuleField.TYPE, "TOP AJÁNLAT HAVIDÍJ"),
          new Rule("Bank fees", RuleField.TYPE, "HAVI CSOMAGDÍJ"),
          new Rule("Bank fees", RuleField.TYPE, "KP.FELVÉT/-BEFIZ. DÍJA"),
          new Rule("Bank fees", RuleField.TYPE, "KÖLTSÉG ÉS JUTALÉK"),
          new Rule("Food Delivery", RuleField.COUNTERPARTY_NAME, "Wolt"),
          new Rule("Revolut", RuleField.COUNTERPARTY_NAME, "Revolut"),
          new Rule("Flights", RuleField.COUNTERPARTY_NAME, "Ryanair"),
          new Rule("Flights", RuleField.COUNTERPARTY_NAME, "Wizz Air"),
          new Rule("Flights", RuleField.COUNTERPARTY_NAME, "klm.com"));

  private DefaultCategories() {}
}
