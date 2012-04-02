package com.bizo.detonator.config;

import java.beans.PropertyDescriptor;

public class DtoProperty {

  private final PropertyDescriptor pd;

  public DtoProperty(final PropertyDescriptor pd) {
    this.pd = pd;
  }

  public String getName() {
    return pd.getName();
  }

  public String getGetterMethodName() {
    return pd.getReadMethod() == null ? null : pd.getReadMethod().getName();
  }

  public String getSetterMethodName() {
    return pd.getWriteMethod() == null ? null : pd.getWriteMethod().getName();
  }

  public Class<?> getType() {
    return pd.getPropertyType();
  }

  public boolean isReadOnly() {
    return pd.getWriteMethod() == null;
  }

  @Override
  public String toString() {
    return pd.getName();
  }
}
