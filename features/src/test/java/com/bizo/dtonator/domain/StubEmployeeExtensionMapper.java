package com.bizo.dtonator.domain;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.bizo.dtonator.mapper.AbstractEmployeeExtensionDtoMapper;

public class StubEmployeeExtensionMapper extends AbstractEmployeeExtensionDtoMapper {

  public List<Integer> extensionValues = newArrayList();

  @Override
  public void extensionValueFromDto(final Employee domain, final Integer value) {
    extensionValues.add(value);
  }

  @Override
  public Integer extensionValueToDto(final Employee domain) {
    return 1;
  }

}
