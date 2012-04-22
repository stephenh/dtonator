package com.bizo.dtonator;

import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBetween;
import static org.apache.commons.lang.StringUtils.uncapitalize;

import com.bizo.dtonator.config.DtoConfig;
import com.bizo.dtonator.config.RootConfig;
import com.bizo.dtonator.config.ValueTypeConfig;

public class Names {

  static String mapperFieldName(final DtoConfig dc) {
    return uncapitalize(dc.getSimpleName()) + "Mapper";
  }

  static String mapperFieldName(final ValueTypeConfig vtc) {
    return vtc.name + "Mapper";
  }

  static String mapperInterface(final RootConfig rc, final DtoConfig dc) {
    return rc.getMapperPackage() + "." + dc.getSimpleName() + "Mapper";
  }

  static String mapperInterface(final RootConfig rc, final ValueTypeConfig vtc) {
    return rc.getMapperPackage() + "." + capitalize(vtc.name) + "Mapper";
  }

  public static String listType(final String type) {
    return substringBetween(type, "<", ">");
  }

  public static String simple(final String type) {
    return substringAfterLast(type, ".");
  }

}
