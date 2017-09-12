package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.GreenHueAccountDto;

public class GreenHueAccountModel extends GreenHueAccountModelCodegen {

  public GreenHueAccountModel(GreenHueAccountDto dto) {
    super(dto);
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
