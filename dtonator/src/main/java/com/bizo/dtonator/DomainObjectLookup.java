package com.bizo.dtonator;

public interface DomainObjectLookup {

  <T> T lookup(final Class<T> type, final Long id);

}
