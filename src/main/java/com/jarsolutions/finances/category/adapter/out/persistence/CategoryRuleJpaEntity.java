package com.jarsolutions.finances.category.adapter.out.persistence;

import com.jarsolutions.finances.category.application.domain.model.RuleField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "category_rule")
class CategoryRuleJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_account_id", nullable = false)
  private Long userId;

  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private RuleField field;

  @Column(nullable = false)
  private String pattern;

  protected CategoryRuleJpaEntity() {}

  CategoryRuleJpaEntity(Long userId, Long categoryId, RuleField field, String pattern) {
    this.userId = userId;
    this.categoryId = categoryId;
    this.field = field;
    this.pattern = pattern;
  }

  Long getId() {
    return id;
  }

  Long getUserId() {
    return userId;
  }

  Long getCategoryId() {
    return categoryId;
  }

  RuleField getField() {
    return field;
  }

  String getPattern() {
    return pattern;
  }
}
