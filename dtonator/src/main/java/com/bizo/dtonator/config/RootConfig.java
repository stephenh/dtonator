package com.bizo.dtonator.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.bizo.dtonator.properties.TypeOracle;

public class RootConfig {

  private static final String configKey = "config";
  private final TypeOracle oracle;
  private final Map<String, Object> root;

  public RootConfig(final TypeOracle oracle, final Object root) {
    this.oracle = oracle;
    this.root = YamlUtils.ensureMap(root);
  }

  public String getDtoPackage() {
    return getConfig().get("dtoPackage");
  }

  public String getDomainPackage() {
    return getConfig().get("domainPackage");
  }

  public String getMapperPackage() {
    return getConfig().get("mapperPackage");
  }

  public Collection<DtoConfig> getDtos() {
    final List<DtoConfig> dtos = newArrayList();
    for (final String simpleName : root.keySet()) {
      if (simpleName.equals(configKey)) {
        continue;
      }
      dtos.add(new DtoConfig(oracle, this, simpleName, root.get(simpleName)));
    }
    return dtos;
  }

  public UserTypeConfig getUserTypeForDomainType(final String domainType) {
    for (final UserTypeConfig utc : getUserTypes()) {
      if (utc.domainType.equals(domainType)) {
        return utc;
      }
    }
    return null;
  }

  public List<UserTypeConfig> getUserTypes() {
    final Object value = getConfig().get("userTypes");
    if (value == null) {
      return newArrayList();
    }
    final List<UserTypeConfig> userTypes = newArrayList();
    for (final Map.Entry<Object, Object> e : YamlUtils.ensureMap(value).entrySet()) {
      userTypes.add(new UserTypeConfig(e));
    }
    return userTypes;
  }

  private Map<String, String> getConfig() {
    return YamlUtils.ensureMap(root.get(configKey));
  }

}
