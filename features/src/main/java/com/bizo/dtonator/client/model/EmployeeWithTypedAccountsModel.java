package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.EmployeeWithTypedAccountsDto;

public class EmployeeWithTypedAccountsModel extends EmployeeWithTypedAccountsModelCodegen {

  public EmployeeWithTypedAccountsModel(EmployeeWithTypedAccountsDto dto) {
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
