package com.bizo.detonator.config;

import java.util.List;
import java.util.Map;

import com.bizo.detonator.properties.TypeOracle;

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
    return oracle.getProperties(getFullDomainName());
  }

  public String getSimpleName() {
    return simpleName;
  }

  public String getFullDomainName() {
    return root.getDomainPackage() + "." + map.get("domain");
  }

  public boolean isEnum() {
    return oracle.isEnum(getFullDomainName());
  }

  public List<String> getEnumValues() {
    return oracle.getEnumValues(getFullDomainName());
  }

}
