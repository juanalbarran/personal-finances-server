package com.jarsolutions.finances.transaction.adapter.in.web;

/**
 * @param categoryId null leaves the transaction without category
 */
record TransactionCategoryRequest(Long categoryId) {}
