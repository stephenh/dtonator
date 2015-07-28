package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.EmployeeTslDto;

public class EmployeeTslModel extends EmployeeTslModelCodegen {

  public EmployeeTslModel(EmployeeTslDto dto) {
    super(dto);
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
