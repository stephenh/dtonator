package com.bizo.detonator.config;

import java.util.Map;

public class YamlUtils {

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> ensureMap(final Object o) {
    if (!(o instanceof Map)) {
      System.err.println("Expecting a map");
      System.exit(1);
    }
    return (Map<K, V>) o;
  }

}
