package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.EmployeeAccountTslDto;

public class EmployeeAccountTslModel extends EmployeeAccountTslModelCodegen {

  public EmployeeAccountTslModel(final EmployeeAccountTslDto dto) {
    addRules();
    merge(dto);
  }

  private void addRules() {
    name.req();
  }

}
