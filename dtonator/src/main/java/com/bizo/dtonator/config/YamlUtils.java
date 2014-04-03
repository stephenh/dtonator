package com.bizo.dtonator.config;

import static joist.util.Copy.list;

import java.util.List;
import java.util.Map;

public class YamlUtils {

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> ensureMap(final Object o) {
    if (!(o instanceof Map)) {
      throw new IllegalArgumentException("Expecting a map: " + o);
    }
    return (Map<K, V>) o;
  }

  public static List<String> parseExpectedStringToList(String key, Object rawValue) {
    if (rawValue == null) {
      return list();
    }
    if (!(rawValue instanceof String)) {
      throw new IllegalStateException("Expecting a string value for key " + key + ": " + rawValue);
    }
    return list(((String) rawValue).split(", ?"));
  }

}
