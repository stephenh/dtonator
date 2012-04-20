package com.bizo.dtonator.config;

import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBetween;

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
    if (p.type == null) {
      throw new IllegalStateException("Type not found for " + p.name);
    }
  }

  public String getDtoType() {
    if (getValueTypeConfig() != null) {
      return getValueTypeConfig().dtoType;
    }
    if (isListOfEntities()) {
      // have they mapped this to a dto?
      final String guessedSimpleName = substringAfterLast(getSingleDomainType(), ".") + "Dto";
      final DtoConfig childConfig = config.getDto(guessedSimpleName);
      if (childConfig == null) {
        throw new IllegalStateException("Could not find a default dto " + guessedSimpleName + " for " + getDomainType());
      }
      // TODO hardcoding j.u.ArrayList for now
      return "java.util.ArrayList<" + childConfig.getDtoType() + ">";
    }
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
    return p.readOnly;
  }

  public boolean isListOfEntities() {
    // TODO should also check if dtoType is java.util.ArrayList? or is that isListOfDtos?
    return getDomainType().startsWith("java.util.List<" + config.getDomainPackage());
  }

  public String getSingleDtoType() {
    // assumes isListOfEntities
    return substringBetween(getDtoType(), "<", ">");
  }

  public String getSimpleSingleDtoType() {
    // assumes isListOfEntities
    return substringAfterLast(getSingleDtoType(), ".");
  }

  public String getSingleDomainType() {
    // assumes isListOfEntities
    return substringBetween(getDomainType(), "<", ">");
  }

  public boolean isValueType() {
    return getValueTypeConfig() != null;
  }

  public boolean isEnum() {
    return oracle.isEnum(getDomainType());
  }

  public ValueTypeConfig getValueTypeConfig() {
    return config.getValueTypeForDomainType(getDomainType());
  }

  /** only meaningful for non-manual dtos */
  public boolean isExtension() {
    return p.getterMethodName == null && p.setterNameMethod == null;
  }

  @Override
  public String toString() {
    return p.name;
  }

}
