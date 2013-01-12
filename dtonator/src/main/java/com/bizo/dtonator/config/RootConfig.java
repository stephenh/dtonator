package com.bizo.dtonator.config;

import static java.lang.Boolean.TRUE;
import static joist.util.Copy.list;
import static org.apache.commons.lang.StringUtils.defaultString;

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

  /** @return the model package, if generating Tessell models */
  public String getModelPackage() {
    return getConfig().get("modelPackage");
  }

  /** @return the model base class, if generating Tessell models, or {@code null} */
  public String getModelBaseClass() {
    return defaultString(getConfig().get("modelBaseClass"), "org.tessell.model.AbstractDtoModel");
  }

  public String getOutputDirectory() {
    return defaultString(getConfig().get("outputDirectory"), "target/gen-java-src");
  }

  public String getSourceDirectory() {
    return defaultString(getConfig().get("sourceDirectory"), "src/main/java");
  }

  public String getIndent() {
    final String indent = defaultString(getConfig().get("indent"), "four-space");
    if ("two-space".equals(indent)) {
      return "  ";
    } else if ("tab".equals(indent)) {
      return "\t";
    } else {
      return "    ";
    }
  }

  public Prune getPrune() {
    final String prune = defaultString(getConfig().get("pruneOldFiles"), "usedPackages");
    if ("usedPackages".equals(prune)) {
      return Prune.USED_PACKAGES;
    } else if ("allPackages".equals(prune)) {
      return Prune.ALL_PACKAGES;
    } else if ("disabled".equals(prune)) {
      return Prune.DISABLED;
    } else {
      throw new IllegalStateException("Unknown pruneOldFiles setting: " + prune);
    }
  }

  public List<String> getCommonInterfaces() {
    if (getConfig().containsKey("commonInterfaces")) {
      return list(getConfig().get("commonInterfaces").split(", ?"));
    }
    return list();
  }

  public boolean includeBeanMethods() {
    return TRUE.equals(getConfig().get("beanMethods"));
  }

  public Collection<DtoConfig> getDtos() {
    if (dtos == null) {
      dtos = list();
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

  public ValueTypeConfig getValueTypeForDtoType(final String dtoType) {
    for (final ValueTypeConfig vtc : getValueTypes()) {
      if (vtc.dtoType.endsWith(dtoType)) { // allow simple class names
        return vtc;
      }
    }
    return null;
  }

  public List<ValueTypeConfig> getValueTypes() {
    final Object value = getConfig().get("valueTypes");
    if (value == null) {
      return list();
    }
    final List<ValueTypeConfig> valueTypes = list();
    for (final Map.Entry<Object, Object> e : YamlUtils.ensureMap(value).entrySet()) {
      valueTypes.add(new ValueTypeConfig(e));
    }
    return valueTypes;
  }

  public String getModelPropertyType(final String dtoType) {
    final Object map = getConfig().get("modelTypes");
    if (map == null) {
      return null;
    }
    return (String) YamlUtils.ensureMap(map).get(dtoType);
  }

  private Map<String, String> getConfig() {
    return YamlUtils.ensureMap(root.get(configKey));
  }

}
