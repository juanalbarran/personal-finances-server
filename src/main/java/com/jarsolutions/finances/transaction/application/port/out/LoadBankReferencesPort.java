package com.jarsolutions.finances.transaction.application.port.out;

import java.util.Collection;
import java.util.Set;

@FunctionalInterface
public interface LoadBankReferencesPort {

  /** The ones of the given bank references that already have a transaction in the database. */
  Set<String> loadExistingBankReferences(Collection<String> bankReferences);
}
