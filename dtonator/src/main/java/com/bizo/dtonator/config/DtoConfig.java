package com.bizo.dtonator.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import com.bizo.dtonator.properties.Prop;
import com.bizo.dtonator.properties.TypeOracle;

public class DtoConfig {

  private final TypeOracle oracle;
  private final RootConfig root;
  private final String simpleName;
  private final Map<String, Object> map;
  private List<DtoProperty> properties;

  public DtoConfig(final TypeOracle oracle, final RootConfig root, final String simpleName, final Object map) {
    this.oracle = oracle;
    this.root = root;
    this.simpleName = simpleName;
    this.map = YamlUtils.ensureMap(map);
  }

  public List<DtoProperty> getProperties() {
    if (properties == null) {
      properties = newArrayList();
      final List<String> pc = getPropertiesConfig();
      final boolean returnAll = pc == null;
      final boolean exclusionMode = !returnAll && hasExclusion(pc);
      for (final Prop p : oracle.getProperties(getDomainType())) {
        if (returnAll //
          || (exclusionMode && !pc.contains("-" + p.name))
          || (!exclusionMode && pc.contains(p.name))) {
          properties.add(new DtoProperty(oracle, root, p));
        }
      }
    }
    return properties;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public String getDtoType() {
    return root.getDtoPackage() + "." + getSimpleName();
  }

  public String getDomainType() {
    final String rawValue = (String) map.get("domain");
    if (rawValue == null) {
      throw new IllegalArgumentException("Missing key domain for " + simpleName);
    }
    if (rawValue.contains(".")) {
      return rawValue;
    } else {
      return root.getDomainPackage() + "." + rawValue;
    }
  }

  public boolean isEnum() {
    return oracle.isEnum(getDomainType());
  }

  public List<String> getEnumValues() {
    return oracle.getEnumValues(getDomainType());
  }

  private List<String> getPropertiesConfig() {
    final Object rawValue = map.get("properties");
    if (rawValue == null) {
      return null;
    }
    if (!(rawValue instanceof String)) {
      throw new IllegalStateException("Expecting a string value for key properties: " + rawValue);
    }
    return newArrayList(((String) rawValue).split(", ?"));
  }

  private static boolean hasExclusion(final List<String> pc) {
    for (final String p : pc) {
      if (p.startsWith("-")) {
        return true;
      }
    }
    return false;
  }

}
