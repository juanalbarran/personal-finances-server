package com.jarsolutions.finances.transaction.application.domain.model;

import java.util.Optional;

/**
 * A transaction of the database.
 *
 * @param categoryId empty while the transaction has no category
 */
public record StoredTransaction(long id, Transaction transaction, Optional<Long> categoryId) {}
