package com.jarsolutions.finances.category.adapter.out.persistence;

import com.jarsolutions.finances.category.application.domain.model.Category;
import com.jarsolutions.finances.category.application.domain.model.CategoryRule;
import org.springframework.stereotype.Component;

@Component
class CategoryMapper {

  Category toDomain(CategoryJpaEntity entity) {
    return new Category(entity.getId(), entity.getUserId(), entity.getName());
  }

  CategoryRule toDomain(CategoryRuleJpaEntity entity) {
    return new CategoryRule(
        entity.getId(),
        entity.getUserId(),
        entity.getCategoryId(),
        entity.getField(),
        entity.getPattern());
  }
}
