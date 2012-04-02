package com.bizo.dtonator.config;

import static org.apache.commons.lang.StringUtils.substringAfterLast;

import com.bizo.dtonator.properties.Prop;
import com.bizo.dtonator.properties.TypeOracle;

/** A property to map back/forth between DTO/domain object. */
public class DtoProperty {

  private final TypeOracle oracle;
  private final RootConfig config;
  private final Prop p;

  public DtoProperty(final TypeOracle oracle, final RootConfig config, final Prop p) {
    this.oracle = oracle;
    this.config = config;
    this.p = p;
  }

  public boolean needsConversion() {
    return !getDomainType().equals(getDtoType());
  }

  public String getDtoType() {
    if (getDomainType().startsWith(config.getDomainPackage())) {
      // in the domain package...just assume we have a dto for it?
      // should probably skip it
      // unless it's an enum
      if (oracle.isEnum(getDomainType())) {
        return config.getDtoPackage() + "." + substringAfterLast(getDomainType(), ".");
      }
    }
    // assume it's java.lang.String/etc. and doesn't need mapped
    return getDomainType();
  }

  public String getName() {
    return p.name;
  }

  public String getDomainType() {
    return p.type;
  }

  public String getGetterMethodName() {
    return p.getterMethodName;
  }

  public String getSetterMethodName() {
    return p.setterNameMethod;
  }

  public boolean isReadOnly() {
    return p.setterNameMethod == null;
  }

  @Override
  public String toString() {
    return p.name;
  }
}
