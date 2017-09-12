package com.bizo.dtonator.properties;

import static joist.util.Copy.list;
import static org.apache.commons.lang.StringUtils.capitalize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StubTypeOracle implements TypeOracle {

  private final Map<String, List<Prop>> properties = new HashMap<String, List<Prop>>();
  private final Map<String, List<String>> enumValues = new HashMap<String, List<String>>();

  @Override
  public List<Prop> getProperties(final String className) {
    final List<Prop> properties = this.properties.get(className);
    if (properties == null) {
      return list();
    }
    return properties;
  }

  @Override
  public boolean isEnum(final String className) {
    return enumValues.containsKey(className);
  }

  @Override
  public boolean isAbstract(String className) {
    return false;
  }

  @Override
  public List<String> getEnumValues(final String className) {
    return enumValues.get(className);
  }

  public void addProperty(final String className, final String name, final String type) {
    List<Prop> properties = this.properties.get(className);
    if (properties == null) {
      properties = list();
      this.properties.put(className, properties);
    }
    properties.add(new Prop(name, type, false, "get" + capitalize(name), "set" + capitalize(name), className, className));
  }

  public void setProperties(final String className, final List<Prop> properties) {
    this.properties.put(className, properties);
  }

  public void setEnumValues(final String className, final List<String> enumValues) {
    this.enumValues.put(className, enumValues);
  }
}
