package com.bizo.dtonator.config;

import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBetween;

import com.bizo.dtonator.Names;
import com.bizo.dtonator.properties.TypeOracle;

/** A property to map back/forth between DTO/domain object. */
public class DtoProperty {

  private final TypeOracle oracle;
  private final RootConfig config;
  private final DtoConfig dto;
  private final String name;
  private final boolean readOnly;
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
    final DtoConfig dto,
    final String name,
    final boolean readOnly,
    final boolean isChainedId,
    final String dtoType,
    final String domainType,
    final String getterMethodName,
    final String setterNameMethod) {
    this.oracle = oracle;
    this.config = config;
    this.dto = dto;
    this.name = name;
    this.readOnly = readOnly;
    this.isChainedId = isChainedId;
    this.dtoType = dtoType;
    this.domainType = domainType;
    this.getterMethodName = getterMethodName;
    this.setterNameMethod = setterNameMethod;
  }

  public DtoConfig getDto() {
    return dto;
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

  public boolean isDto() {
    return config.getDto(dtoType) != null && !config.getDto(dtoType).isEnum();
  }

  public boolean isList() {
    return dtoType.startsWith("java.util.ArrayList");
  }

  public boolean isListOfEntities() {
    return DtoConfig.isListOfEntities(config, domainType);
  }

  public boolean isListOfDtos() {
    return DtoConfig.isListOfDtos(config, dtoType);
  }

  public String getSingleDtoType() {
    // assumes isListOfEntities
    return substringBetween(getDtoType(), "<", ">");
  }

  public String getSimpleSingleDtoType() {
    // assumes isListOfEntities
    return substringAfterLast(getSingleDtoType(), ".");
  }

  public DtoConfig getSingleDto() {
    // assumes isListOfEntities (or manual isListOfDtos)
    return config.getDto(getSimpleSingleDtoType());
  }

  public String getSingleDomainType() {
    // assumes isListOfEntities
    return Names.listType(domainType);
  }

  public boolean isValueType() {
    return getValueTypeConfig() != null;
  }

  public boolean isEnum() {
    if (config.getDto(getDtoType()) != null) {
      return config.getDto(getDtoType()).isEnum();
    }
    return oracle.isEnum(getDomainType());
  }

  public ValueTypeConfig getValueTypeConfig() {
    return config.getValueTypeForDomainType(getDomainType());
  }

  /** only meaningful for non-manual dtos, otherwise everything is manual... */
  public boolean isExtension() {
    return (getterMethodName == null && setterNameMethod == null && !isChainedId()) //
      || dto.getForcedMappers().contains(name);
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

  public boolean isChainedId() {
    return isChainedId;
  }

}
