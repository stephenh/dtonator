package com.bizo.dtonator.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

class Primitives {

  private static Map<String, String> p = new HashMap<String, String>();

  static {
    p.put("boolean", "java.lang.Boolean");
    p.put("int", "java.lang.Integer");
    p.put("long", "java.lang.Long");
    p.put("double", "java.lang.Double");
    p.put("byte", "java.lang.Byte");
    p.put("short", "java.lang.Short");
    p.put("float", "java.lang.Float");
    p.put("char", "java.lang.Char");
  }

  static String boxIfNecessary(final String type) {
    return StringUtils.defaultString(p.get(type), type);
  }

}
