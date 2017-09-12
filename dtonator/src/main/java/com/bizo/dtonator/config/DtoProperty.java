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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((domainType == null) ? 0 : domainType.hashCode());
    result = prime * result + ((dtoType == null) ? 0 : dtoType.hashCode());
    result = prime * result + ((getterMethodName == null) ? 0 : getterMethodName.hashCode());
    result = prime * result + (isChainedId ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + (readOnly ? 1231 : 1237);
    result = prime * result + ((setterNameMethod == null) ? 0 : setterNameMethod.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DtoProperty other = (DtoProperty) obj;
    if (domainType == null) {
      if (other.domainType != null)
        return false;
    } else if (!domainType.equals(other.domainType))
      return false;
    if (dtoType == null) {
      if (other.dtoType != null)
        return false;
    } else if (!dtoType.equals(other.dtoType))
      return false;
    if (getterMethodName == null) {
      if (other.getterMethodName != null)
        return false;
    } else if (!getterMethodName.equals(other.getterMethodName))
      return false;
    if (isChainedId != other.isChainedId)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (readOnly != other.readOnly)
      return false;
    if (setterNameMethod == null) {
      if (other.setterNameMethod != null)
        return false;
    } else if (!setterNameMethod.equals(other.setterNameMethod))
      return false;
    return true;
  }

}
