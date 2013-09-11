package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.EmployeeWithTypeDto;

public class EmployeeWithTypeModel extends EmployeeWithTypeModelCodegen {

  public EmployeeWithTypeModel(EmployeeWithTypeDto dto) {
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
