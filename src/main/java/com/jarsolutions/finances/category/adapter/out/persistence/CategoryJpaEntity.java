package com.jarsolutions.finances.category.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "category")
class CategoryJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_account_id", nullable = false)
  private Long userId;

  @Column(nullable = false, length = 100)
  private String name;

  protected CategoryJpaEntity() {}

  CategoryJpaEntity(Long userId, String name) {
    this.userId = userId;
    this.name = name;
  }

  void rename(String name) {
    this.name = name;
  }

  Long getId() {
    return id;
  }

  Long getUserId() {
    return userId;
  }

  String getName() {
    return name;
  }
}
