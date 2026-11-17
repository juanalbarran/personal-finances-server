package com.jarsolutions.finances.user.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_account")
class UserJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  protected UserJpaEntity() {}

  UserJpaEntity(String username, String email, String name, String lastName) {
    this.username = username;
    this.email = email;
    this.name = name;
    this.lastName = lastName;
  }

  void changeProfile(String email, String name, String lastName) {
    this.email = email;
    this.name = name;
    this.lastName = lastName;
  }

  Long getId() {
    return id;
  }

  String getUsername() {
    return username;
  }

  String getEmail() {
    return email;
  }

  String getName() {
    return name;
  }

  String getLastName() {
    return lastName;
  }
}
