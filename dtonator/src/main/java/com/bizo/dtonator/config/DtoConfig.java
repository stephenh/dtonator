package com.bizo.dtonator.config;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang.StringUtils.defaultString;

import java.util.*;

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
      final List<PropConfig> pcs = getPropertiesConfig();
      if (getDomainType() != null) {
        final boolean includeUnmapped = pcs.size() == 0 || hasPropertiesInclude();
        for (final Prop p : oracle.getProperties(getDomainType())) {
          final PropConfig pc = findPropConfig(pcs, p.name);
          // if we found a property in the oracle, we know this isn't an extension
          if (pc != null) {
            pc.markMapped();
          }
          final boolean doNotMap =
            (pc != null && pc.isExclusion)
              || (pc == null && !includeUnmapped)
              || (pc == null && p.type.startsWith("java.util.List"));
          if (doNotMap) {
            continue;
          }
          if (pc != null) {
            // allow user to override the type
            properties.add(new DtoProperty(oracle, root, new Prop(
              p.name,
              pc.type != null ? pc.type : p.type,
              pc.isReadOnly,
              pc.type != null ? null : p.getterMethodName,
              pc.type != null || pc.isReadOnly ? null : p.setterNameMethod)));
          } else {
            properties.add(new DtoProperty(oracle, root, p));
          }
        }
        // now look for extension properties
        for (final PropConfig pc : pcs) {
          if (!pc.mapped) {
            properties.add(new DtoProperty(oracle, root, new Prop(pc.name, pc.type, pc.isReadOnly, null, null)));
          }
        }
      } else if (pcs.size() > 0) {
        // this is a manual dto
        for (final PropConfig pc : pcs) {
          properties.add(new DtoProperty(oracle, root, new Prop(pc.name, pc.type, pc.isReadOnly, null, null)));
        }
      }
      Collections.sort(properties, new Comparator<DtoProperty>() {
        public int compare(final DtoProperty o1, final DtoProperty o2) {
          final PropConfig pc1 = findPropConfig(pcs, o1.getName());
          final PropConfig pc2 = findPropConfig(pcs, o2.getName());
          if (pc1 != null && pc2 != null) {
            return indexOfPropConfig(pcs, o1.getName()) - indexOfPropConfig(pcs, o2.getName());
          } else if (pc1 != null && pc2 == null) {
            return -1;
          } else if (pc1 == null && pc2 != null) {
            return 1;
          } else {
            return o1.getName().compareTo(o2.getName());
          }
        }
      });
    }
    return properties;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public String getDtoType() {
    return root.getDtoPackage() + "." + getSimpleName();
  }

  public boolean shouldAddPublicConstructor() {
    return TRUE.equals(map.get("publicConstructor"));
  }

  public List<String> getAnnotations() {
    final List<String> annotations = newArrayList();
    if (map.get("annotations") != null) {
      for (final String annotation : defaultString((String) map.get("annotations")).split(", ?")) {
        annotations.add("@" + annotation);
      }
    }
    return annotations;
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

  @Override
  public String toString() {
    return getSimpleName();
  }

  /** @return the `properties: a, b` as parsed {@link PropConfig}, skipping the `*` character. */
  private List<PropConfig> getPropertiesConfig() {
    final List<PropConfig> args = newArrayList();
    for (final String eachValue : getPropertiesConfigRaw()) {
      if (!"*".equals(eachValue)) {
        args.add(new PropConfig(eachValue));
      }
    }
    return args;
  }

  private boolean hasPropertiesInclude() {
    return getPropertiesConfigRaw().contains("*");
  }

  private List<String> getPropertiesConfigRaw() {
    final Object rawValue = map.get("properties");
    if (rawValue == null) {
      return newArrayList();
    }
    if (!(rawValue instanceof String)) {
      throw new IllegalStateException("Expecting a string value for key properties: " + rawValue);
    }
    return newArrayList(((String) rawValue).split(", ?"));
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
      // TODO need to recursively handle generics
      // TODO consult the oracle to see if the type exists
      if (type.startsWith("ArrayList")) {
        type = "java.util." + type;
      } else {
        type = "java.lang." + type;
      }
    }
    return new String[] { name, type };
  }

  /** Small abstraction around the property strings in the YAML file. */
  private static class PropConfig {
    private final String name;
    private final String type;
    private final boolean isExclusion;
    private final boolean isReadOnly;
    private boolean mapped = false;

    private PropConfig(String value) {
      if (value.startsWith("~")) {
        isReadOnly = true;
        value = value.substring(1);
      } else {
        isReadOnly = false;
      }
      if (value.contains(" ")) {
        final String[] parts = splitIntoNameAndType(value);
        name = parts[0];
        type = parts[1];
        isExclusion = false;
      } else if (value.startsWith("-")) {
        name = value.substring(1);
        type = null;
        isExclusion = true;
      } else {
        name = value;
        type = null;
        isExclusion = false;
      }
    }

    private void markMapped() {
      mapped = true;
    }

    @Override
    public String toString() {
      return (isExclusion ? "-" : "") + name;
    }
  }

  private static PropConfig findPropConfig(final List<PropConfig> pcs, final String name) {
    for (final PropConfig pc : pcs) {
      if (pc.name.equals(name)) {
        return pc;
      }
    }
    return null;
  }

  private static int indexOfPropConfig(final List<PropConfig> pcs, final String name) {
    int i = 0;
    for (final PropConfig pc : pcs) {
      if (pc.name.equals(name)) {
        return i;
      }
      i++;
    }
    return -1;
  }
}
