package com.bizo.dtonator.config;

import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBetween;

import com.bizo.dtonator.Names;
import com.bizo.dtonator.properties.TypeOracle;

/** A property to map back/forth between DTO/domain object. */
public class DtoProperty {

  private final TypeOracle oracle;
  private final RootConfig config;
  private final String name;
  private final boolean readOnly;
  private final boolean isRecursive;
  private final boolean isChainedId;
  /** could be String, Money (value type), Employer, List<Employer>. */
  private final String domainType;
  /** could be String, Money (value type), EmployerDto, ArrayList<EmployerDto> */
  private final String dtoType;
  private final String getterMethodName;
  private final String setterNameMethod;

  public DtoProperty(
    final TypeOracle oracle,
    final RootConfig config,
    final String name,
    final boolean readOnly,
    final boolean isRecursive,
    final boolean isChainedId,
    final String dtoType,
    final String domainType,
    final String getterMethodName,
    final String setterNameMethod) {
    this.oracle = oracle;
    this.config = config;
    this.name = name;
    this.readOnly = readOnly;
    this.isRecursive = isRecursive;
    this.isChainedId = isChainedId;
    this.dtoType = dtoType;
    this.domainType = domainType;
    this.getterMethodName = getterMethodName;
    this.setterNameMethod = setterNameMethod;
  }

  public String getDtoType() {
    return dtoType;
  }

  public String getDtoTypeBoxed() {
    return Primitives.boxIfNecessary(dtoType);
  }

  public boolean isEntity() {
    return DtoConfig.isEntity(config, domainType);
  }

  public boolean isList() {
    return dtoType.startsWith("java.util.ArrayList");
  }

  public boolean isListOfEntities() {
    return DtoConfig.isListOfEntities(config, domainType);
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
    return Names.listType(domainType);
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

  /** only meaningful for non-manual dtos, otherwise everything is manual... */
  public boolean isExtension() {
    return getterMethodName == null && setterNameMethod == null && !isChainedId();
  }

  @Override
  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }

  public String getDomainType() {
    return domainType;
  }

  public String getGetterMethodName() {
    return getterMethodName;
  }

  public String getSetterMethodName() {
    return setterNameMethod;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public boolean isRecursive() {
    return isRecursive;
  }

  public boolean isChainedId() {
    return isChainedId;
  }

}
