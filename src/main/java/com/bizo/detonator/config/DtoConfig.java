package com.bizo.detonator.config;

import static com.google.common.collect.Lists.newArrayList;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

public class DtoConfig {

  private final RootConfig root;
  private final String simpleName;
  private final Map<String, Object> map;

  public DtoConfig(final RootConfig root, final String simpleName, final Object map) {
    this.root = root;
    this.simpleName = simpleName;
    this.map = YamlUtils.ensureMap(map);
  }

  public String getFullName() {
    return root.getDtoPackage() + "." + getSimpleName();
  }

  public List<DtoProperty> getDomainDescriptors() {
    // Do we have to sort these for determinism?
    final List<DtoProperty> pds = newArrayList();
    for (final PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(getDomainClass())) {
      if (pd.getName().equals("class") || pd.getName().equals("declaringClass")) {
        continue;
      }
      pds.add(new DtoProperty(pd));
    }
    return pds;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public String getFullDomainName() {
    return root.getDomainPackage() + "." + map.get("domain");
  }

  public Class<?> getDomainClass() {
    try {
      return Class.forName(getFullDomainName());
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException("Domain object not found " + getFullDomainName());
    }
  }
}
