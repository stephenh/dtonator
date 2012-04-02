package com.bizo.detonator.properties;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang.StringUtils.capitalize;

import java.util.List;
import java.util.Map;

public class StubTypeOracle implements TypeOracle {

  private final Map<String, List<Prop>> properties = newHashMap();
  private final Map<String, List<String>> enumValues = newHashMap();

  @Override
  public List<Prop> getProperties(final String className) {
    final List<Prop> properties = this.properties.get(className);
    if (properties == null) {
      throw new IllegalArgumentException("No properties configured for " + className);
    }
    return properties;
  }

  @Override
  public boolean isEnum(final String className) {
    return enumValues.containsKey(className);
  }

  @Override
  public List<String> getEnumValues(final String className) {
    return enumValues.get(className);
  }

  public void addProperty(final String className, final String name, final String type) {
    List<Prop> properties = this.properties.get(className);
    if (properties == null) {
      properties = newArrayList();
      this.properties.put(className, properties);
    }
    properties.add(new Prop(name, type, "get" + capitalize(name), "set" + capitalize(name)));
  }

  public void setProperties(final String className, final List<Prop> properties) {
    this.properties.put(className, properties);
  }

  public void setEnumValues(final String className, final List<String> enumValues) {
    this.enumValues.put(className, enumValues);
  }
}
