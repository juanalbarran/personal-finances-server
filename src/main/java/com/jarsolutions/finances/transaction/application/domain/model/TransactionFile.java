package com.jarsolutions.finances.transaction.application.domain.model;

import java.time.Instant;

/**
 * A transaction file that was loaded into the database.
 *
 * @param checksum SHA-256 of the file content, in hexadecimal
 * @param transactionCount how many transactions the load stored
 */
public record TransactionFile(
    TransactionFileName name, String checksum, Instant loadedAt, int transactionCount) {}
