package com.bizo.dtonator.mapper;

import com.bizo.dtonator.domain.Employee;

public class DefaultEmployeeExtensionDtoMapper implements EmployeeExtensionDtoMapper {

  @Override
  public void extensionValueFromDto(final Employee domain, final Integer value) {
  }

  @Override
  public Integer extensionValueToDto(final Employee domain) {
    return 1;
  }

}
