package com.bizo.detonator.properties;

import static com.google.common.collect.Lists.newArrayList;

import java.beans.PropertyDescriptor;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.bizo.detonator.config.DtoProperty;

public class ReflectionTypeOracle implements TypeOracle {

  @Override
  public List<DtoProperty> getProperties(final String className) {
    // Do we have to sort these for determinism?
    final List<DtoProperty> pds = newArrayList();
    for (final PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(getClass(className))) {
      if (pd.getName().equals("class") || pd.getName().equals("declaringClass")) {
        continue;
      }
      pds.add(new DtoProperty(pd));
    }
    return pds;
  }

  @Override
  public boolean isEnum(final String className) {
    return getClass(className).isEnum();
  }

  @Override
  public List<String> getEnumValues(final String className) {
    final List<String> values = newArrayList();
    for (final Object o : getClass(className).getEnumConstants()) {
      final Enum<?> e = (Enum<?>) o;
      values.add(e.name());
    }
    return values;
  }

  private Class<?> getClass(final String className) {
    try {
      return Class.forName(className);
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
