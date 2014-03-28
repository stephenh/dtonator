package com.bizo.dtonator.properties;

import static joist.util.Copy.list;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

public class ReflectionTypeOracle implements TypeOracle {

  @Override
  public List<Prop> getProperties(final String className) {
    // Do we have to sort these for determinism?
    final List<Prop> ps = list();
    for (final PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(getClass(className))) {
      if (pd.getName().equals("class") || pd.getName().equals("declaringClass")) {
        continue;
      }
      ps.add(new Prop( //
        pd.getName(),
        pd.getReadMethod() == null ? pd.getPropertyType().getName() : pd.getReadMethod().getGenericReturnType().toString().replaceAll("^class ", ""),
        pd.getWriteMethod() == null,
        pd.getReadMethod() == null ? null : pd.getReadMethod().getName(),
        pd.getWriteMethod() == null ? null : pd.getWriteMethod().getName()));
    }
    return ps;
  }

  @Override
  public boolean isEnum(final String className) {
    try {
      return getClass(className).isEnum();
    } catch (final IllegalArgumentException iae) {
      return false; // for primitives like boolean
    }
  }

  @Override
  public boolean isAbstract(String className) {
    try {
      return Modifier.isAbstract(getClass(className).getModifiers());
    } catch (final IllegalArgumentException iae) {
      return false; // for primitives like boolean
    }
  }

  @Override
  public List<String> getEnumValues(final String className) {
    final List<String> values = list();
    for (final Object o : getClass(className).getEnumConstants()) {
      final Enum<?> e = (Enum<?>) o;
      values.add(e.name());
    }
    return values;
  }

  private static Class<?> getClass(final String className) {
    try {
      return Class.forName(className);
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
