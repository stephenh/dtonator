package com.bizo.dtonator;

/**
 * Allows looking up entities by id during the mapping process.
 * 
 * Currently assumes {@code Long} ids.
 * 
 * This is the only type, currently, that is required at runtime from the dtonator codebase. Consider splitting out into
 * a separate runtime jar.
 */
public interface DomainObjectLookup {

  <T> T lookup(final Class<T> type, final Long id);

}
