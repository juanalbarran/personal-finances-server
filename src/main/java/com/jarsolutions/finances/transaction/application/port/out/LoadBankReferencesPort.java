package com.jarsolutions.finances.transaction.application.port.out;

import java.util.Collection;
import java.util.Set;

@FunctionalInterface
public interface LoadBankReferencesPort {

  /** The ones of the given bank references the user already has a transaction for. */
  Set<String> loadExistingBankReferences(long userId, Collection<String> bankReferences);
}
