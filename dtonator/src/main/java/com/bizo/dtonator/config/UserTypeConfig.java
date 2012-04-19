package com.bizo.dtonator.config;

import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.uncapitalize;

import java.util.Map;

public class UserTypeConfig {

  public final String name;
  public final String domainType;
  public final String dtoType;

  public UserTypeConfig(final Map.Entry<Object, Object> e) {
    domainType = (String) e.getKey();
    dtoType = (String) e.getValue();
    name = uncapitalize(substringAfterLast(domainType, "."));
  }

}
