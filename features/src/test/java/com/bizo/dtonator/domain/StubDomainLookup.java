package com.bizo.dtonator.domain;

import java.util.HashMap;
import java.util.Map;

import com.bizo.dtonator.DomainObjectLookup;

final class StubDomainLookup implements DomainObjectLookup {

  private final Map<String, Object> objects = new HashMap<String, Object>();

  public void store(final Long id, final Object instance) {
    objects.put(key(instance.getClass(), id), instance);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T lookup(final Class<T> type, final Long id) {
    final T instance = (T) objects.get(key(type, id));
    if (instance == null) {
      throw new IllegalStateException("Instance not found " + type + "#" + id);
    }
    return instance;
  }

  private static String key(final Class<?> type, final Long id) {
    return type + "#" + id;
  }

}
