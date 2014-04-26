package com.bizo.dtonator;

import java.util.HashMap;
import java.util.Map;

/** Tracks new DTOs -> new domain objects so we can avoid making dups. */
public class DomainObjectContext {

  private static final ThreadLocal<DomainObjectContext> context = new ThreadLocal<DomainObjectContext>();
  private final Map<Object, Object> objects = new HashMap<Object, Object>();
  private int outstanding;

  public static DomainObjectContext push() {
    DomainObjectContext c = context.get();
    if (c == null) {
      c = new DomainObjectContext();
      context.set(c);
    }
    c.outstanding++;
    return c;
  }

  public void pop() {
    outstanding--;
    if (outstanding == 0) {
      context.set(null);
    }
  }

  public void store(final Object dto, final Object domain) {
    objects.put(dto, domain);
  }

  public Object get(final Object dto) {
    return objects.get(dto);
  }

}
