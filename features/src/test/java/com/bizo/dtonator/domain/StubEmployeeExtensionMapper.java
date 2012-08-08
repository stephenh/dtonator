package com.bizo.dtonator.domain;

import static joist.util.Copy.list;

import java.util.List;

import com.bizo.dtonator.mapper.EmployeeExtensionDtoMapper;
import com.bizo.dtonator.mapper.Mapper;

public class StubEmployeeExtensionMapper implements EmployeeExtensionDtoMapper {

  public List<Integer> extensionValues = list();

  @Override
  public Integer getExtensionValue(final Mapper mapper, final Employee domain) {
    return 1;
  }

  @Override
  public void setExtensionValue(final Mapper mapper, final Employee domain, final Integer value) {
    extensionValues.add(value);
  }

}
