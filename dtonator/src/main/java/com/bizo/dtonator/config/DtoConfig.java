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

  public DtoConfig(final TypeOracle oracle, final RootConfig root, final String simpleName, final Object map) {
    this.oracle = oracle;
    this.root = root;
    this.simpleName = simpleName;
    this.map = YamlUtils.ensureMap(map);
  }

  public String getFullName() {
    return root.getDtoPackage() + "." + getSimpleName();
  }

  public List<DtoProperty> getProperties() {
    final List<DtoProperty> dps = newArrayList();
    for (final Prop p : oracle.getProperties(getFullDomainName())) {
      dps.add(new DtoProperty(oracle, root, p));
    }
    return dps;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public String getFullDomainName() {
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
    return oracle.isEnum(getFullDomainName());
  }

  public List<String> getEnumValues() {
    return oracle.getEnumValues(getFullDomainName());
  }

}
