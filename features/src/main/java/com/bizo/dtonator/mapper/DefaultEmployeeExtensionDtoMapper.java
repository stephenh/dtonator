package com.bizo.dtonator.mapper;

import com.bizo.dtonator.domain.Employee;

public class DefaultEmployeeExtensionDtoMapper implements EmployeeExtensionDtoMapper {

  @Override
  public Integer getExtensionValue(final Mapper mapper, final Employee domain) {
    return 1;
  }

  @Override
  public void setExtensionValue(final Mapper mapper, final Employee domain, final Integer value) {
  }

}
