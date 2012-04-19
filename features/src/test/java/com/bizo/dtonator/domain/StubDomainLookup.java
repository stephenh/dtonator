package com.bizo.dtonator.domain;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.bizo.dtonator.DomainObjectLookup;

final class StubDomainLookup implements DomainObjectLookup {

  private final Map<String, Object> objects = newHashMap();

  public void store(final Long id, final Object instance) {
    objects.put(key(instance.getClass(), id), instance);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T lookup(final Class<T> type, final Long id) {
    return (T) objects.get(key(type, id));
  }

  private static String key(final Class<?> type, final Long id) {
    return type + "#" + id;
  }

}