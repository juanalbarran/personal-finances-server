package com.jarsolutions.finances.category.adapter.in.web;

import com.jarsolutions.finances.category.application.domain.model.RuleField;

record CategoryRuleRequest(Long categoryId, RuleField field, String pattern) {}
