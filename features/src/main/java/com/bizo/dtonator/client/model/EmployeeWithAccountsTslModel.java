package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.EmployeeWithAccountsTslDto;

public class EmployeeWithAccountsTslModel extends EmployeeWithAccountsTslModelCodegen {

  public EmployeeWithAccountsTslModel(EmployeeWithAccountsTslDto dto) {
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
