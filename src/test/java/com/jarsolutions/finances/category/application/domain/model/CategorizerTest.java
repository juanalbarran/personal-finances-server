package com.jarsolutions.finances.category.application.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CategorizerTest {

  private static final long GROCERIES = 1;
  private static final long FOOD_DELIVERY = 2;
  private static final long BANK_FEES = 3;
  private static final long CAFFES = 4;

  private final Categorizer categorizer =
      new Categorizer(
          List.of(
              rule(1, GROCERIES, RuleField.BANK_CATEGORY, "Bevásárlás"),
              rule(2, BANK_FEES, RuleField.TYPE, "HAVI CSOMAGDÍJ"),
              rule(3, FOOD_DELIVERY, RuleField.COUNTERPARTY_NAME, "wolt"),
              rule(4, GROCERIES, RuleField.COUNTERPARTY_NAME, "Roast"),
              rule(5, CAFFES, RuleField.COUNTERPARTY_NAME, "Light Roast Coffee")));

  @Test
  void theBankCategoryAndTheTypeAreComparedWhole_neverMindingTheCase() {
    assertThat(categorizer.categoryOf(new Spend("SPAR", "VÁSÁRLÁS KÁRTYÁVAL", "bevásárlás")))
        .contains(GROCERIES);
    assertThat(categorizer.categoryOf(new Spend("SPAR", "VÁSÁRLÁS KÁRTYÁVAL", "Bevásárlás 2")))
        .isEmpty();
    assertThat(categorizer.categoryOf(new Spend("FEE", "Havi csomagdíj", "Számlák, rezsi")))
        .contains(BANK_FEES);
  }

  @Test
  void theCounterpartyNameOnlyHasToContainThePattern_andWinsOverTheBankCategory() {
    assertThat(categorizer.categoryOf(new Spend("WOLT", "VÁSÁRLÁS KÁRTYÁVAL", "Bevásárlás")))
        .contains(FOOD_DELIVERY);
    assertThat(categorizer.categoryOf(new Spend("Wolt Budapest", "VÁSÁRLÁS KÁRTYÁVAL", "Egyéb")))
        .contains(FOOD_DELIVERY);
  }

  @Test
  void theLongestPatternWinsAmongTheRulesOfAField() {
    assertThat(
            categorizer.categoryOf(
                new Spend("LIGHT ROAST COFFEE", "VÁSÁRLÁS KÁRTYÁVAL", "Bevásárlás")))
        .contains(CAFFES);
  }

  @Test
  void whenNoRuleMatches_thereIsNoCategory() {
    assertThat(categorizer.categoryOf(new Spend("OTP", "KÉSZPÉNZFELVÉT ATM-BŐL", "Készpénz")))
        .isEmpty();
    assertThat(new Categorizer(List.of()).categoryOf(new Spend("WOLT", "x", "Bevásárlás")))
        .isEmpty();
  }

  private static CategoryRule rule(long id, long categoryId, RuleField field, String pattern) {
    return new CategoryRule(id, 7, categoryId, field, pattern);
  }
}
