package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.GreenAccountDto;

public class GreenAccountModel extends GreenAccountModelCodegen {

  public GreenAccountModel(GreenAccountDto dto) {
    super(dto);
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
