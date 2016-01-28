package com.bizo.dtonator.config;

public class TypeUtils {

  public static String formatForCodeGeneration(final Object value, final Class<?> type) {
    if (type.equals(Integer.class)) {
      return value.toString();
    } else if (type.equals(Long.class)) {
      return value.toString() + "L";
    } else {
      return "\"" + value.toString() + "\"";
    }
  }

}
