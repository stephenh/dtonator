package com.bizo.dtonator.config;

import static joist.util.Copy.list;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeName {

  private static final List<String> javaLangTypes = list("String", "Integer", "Boolean", "Long", "Double");
  private static final List<String> javaUtilTypes = list("ArrayList", "HashMap", "List", "Map");
  private static final Pattern types = Pattern.compile("[A-Za-z0-9\\.]+");

  // Resolves String -> java.lang.String, ArrayList<Integer> -> java.util.ArrayList<java.lang.Integer>
  public static String javaLangOrUtilPrefixIfNecessary(final String type) {
    final Matcher m = types.matcher(type);
    final StringBuffer sb = new StringBuffer();
    while (m.find()) {
      final String part = m.group(0);
      final String replace;
      if (javaLangTypes.contains(part)) {
        replace = "java.lang." + part;
      } else if (javaUtilTypes.contains(part)) {
        replace = "java.util." + part;
      } else {
        replace = part;
      }
      m.appendReplacement(sb, replace);
    }
    m.appendTail(sb);
    return sb.toString();
  }

}
