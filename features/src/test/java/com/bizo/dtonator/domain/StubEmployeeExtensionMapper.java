package com.bizo.dtonator.domain;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.bizo.dtonator.mapper.EmployeeExtensionDtoMapper;

public class StubEmployeeExtensionMapper implements EmployeeExtensionDtoMapper {

  public List<Integer> extensionValues = newArrayList();

  @Override
  public Integer getExtensionValue(final Employee domain) {
    return 1;
  }

  @Override
  public void setExtensionValue(final Employee domain, final Integer value) {
    extensionValues.add(value);
  }

}
