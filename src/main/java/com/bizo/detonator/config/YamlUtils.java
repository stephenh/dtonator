package com.bizo.detonator.config;

import java.util.Map;

public class YamlUtils {

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> ensureMap(final Object o) {
    if (!(o instanceof Map)) {
      throw new IllegalArgumentException("Expecting a map: " + o);
    }
    return (Map<K, V>) o;
  }

}
