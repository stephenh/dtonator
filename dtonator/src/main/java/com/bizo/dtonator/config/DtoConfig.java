package com.bizo.dtonator.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
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
      if (getDomainType() != null) {
        final boolean returnAll = pc == null;
        final boolean exclusionMode = !returnAll && hasExclusion(pc);
        for (final Prop p : oracle.getProperties(getDomainType())) {
          if (returnAll //
            || (exclusionMode && !pc.contains("-" + p.name))
            || (!exclusionMode && pc.contains(p.name))) {
            // TODO Support a "id Long" type override here
            properties.add(new DtoProperty(oracle, root, p));
          }
          if (pc != null) {
            pc.remove(p.name);
            pc.remove("-" + p.name);
          }
        }
        if (pc != null) {
          // now look for extension properties
          for (final String p : pc) {
            final String[] parts = splitIntoNameAndType(p);
            properties.add(new DtoProperty(oracle, root, new Prop(parts[0], parts[1], null, null)));
          }
        }
      } else if (pc != null) {
        for (final String p : pc) {
          final String[] parts = splitIntoNameAndType(p);
          properties.add(new DtoProperty(oracle, root, new Prop(parts[0], parts[1], null, null)));
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
      return null;
    }
    if (rawValue.contains(".")) {
      return rawValue;
    } else {
      return root.getDomainPackage() + "." + rawValue;
    }
  }

  public List<String> getEquality() {
    final Object value = map.get("equality");
    if (value == null) {
      return null;
    }
    if (!(value instanceof String)) {
      throw new IllegalArgumentException("Expected a string for equality key");
    } else {
      return Arrays.asList(((String) value).split(" "));
    }
  }

  public boolean isEnum() {
    return !isManualDto() && oracle.isEnum(getDomainType());
  }

  public List<String> getEnumValues() {
    return oracle.getEnumValues(getDomainType());
  }

  public boolean isManualDto() {
    return getDomainType() == null;
  }

  public boolean hasExtensionProperties() {
    for (final DtoProperty p : getProperties()) {
      if (p.isExtension()) {
        return true;
      }
    }
    return false;
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
    boolean foundExclusion = false;
    boolean foundInclusion = false;
    for (final String p : pc) {
      if (p.startsWith("-")) {
        foundExclusion = true;
      } else {
        foundInclusion = true;
      }
    }
    if (foundInclusion && foundExclusion) {
      throw new IllegalArgumentException("Can't mix inclusions and exclusions: " + pc);
    }
    return foundExclusion;
  }

  private static String[] splitIntoNameAndType(final String value) {
    final String[] parts = value.split(" ");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Value '<name> <type>': " + value);
    }
    final String name = parts[0];
    String type = parts[1];
    // add java.lang prefix? what about domain types?
    if (type.indexOf(".") == -1 && type.matches("^[A-Z].*")) {
      type = "java.lang." + type;
    }
    return new String[] { name, type };
  }
}
