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
  private List<DtoConfig> dtos = null;

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
    if (dtos == null) {
      dtos = newArrayList();
      for (final String simpleName : root.keySet()) {
        if (simpleName.equals(configKey)) {
          continue;
        }
        dtos.add(new DtoConfig(oracle, this, simpleName, root.get(simpleName)));
      }
    }
    return dtos;
  }

  public DtoConfig getDto(final String simpleName) {
    for (final DtoConfig dto : getDtos()) {
      if (dto.getSimpleName().equals(simpleName)) {
        return dto;
      }
    }
    return null;
  }

  public ValueTypeConfig getValueTypeForDomainType(final String domainType) {
    for (final ValueTypeConfig vtc : getValueTypes()) {
      if (vtc.domainType.equals(domainType)) {
        return vtc;
      }
    }
    return null;
  }

  public List<ValueTypeConfig> getValueTypes() {
    final Object value = getConfig().get("valueTypes");
    if (value == null) {
      return newArrayList();
    }
    final List<ValueTypeConfig> valueTypes = newArrayList();
    for (final Map.Entry<Object, Object> e : YamlUtils.ensureMap(value).entrySet()) {
      valueTypes.add(new ValueTypeConfig(e));
    }
    return valueTypes;
  }

  private Map<String, String> getConfig() {
    return YamlUtils.ensureMap(root.get(configKey));
  }

}
