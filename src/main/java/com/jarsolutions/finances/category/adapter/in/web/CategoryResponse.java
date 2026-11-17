package com.jarsolutions.finances.category.adapter.in.web;

import com.jarsolutions.finances.category.application.domain.model.Category;

record CategoryResponse(long id, String name) {

  static CategoryResponse from(Category category) {
    return new CategoryResponse(category.id(), category.name());
  }
}
