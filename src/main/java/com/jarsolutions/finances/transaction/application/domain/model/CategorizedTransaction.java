package com.jarsolutions.finances.transaction.application.domain.model;

import java.util.Optional;

/**
 * A transaction on its way to the database.
 *
 * @param categoryId the category the rules of the user chose, empty when no rule matched
 */
public record CategorizedTransaction(Transaction transaction, Optional<Long> categoryId) {}
