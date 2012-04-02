package com.bizo.detonator.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RootConfig {

  private static final String configKey = "config";
  private final Map<String, Object> root;

  public RootConfig(final Object root) {
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
      dtos.add(new DtoConfig(this, simpleName, root.get(simpleName)));
    }
    return dtos;
  }

  private Map<String, String> getConfig() {
    return YamlUtils.ensureMap(root.get(configKey));
  }

}
