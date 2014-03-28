package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.RedAccountDto;

public class RedAccountModel extends RedAccountModelCodegen {

  public RedAccountModel(RedAccountDto dto) {
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
