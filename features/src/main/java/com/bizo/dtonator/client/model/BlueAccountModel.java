package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.BlueAccountDto;

public class BlueAccountModel extends BlueAccountModelCodegen {

  public BlueAccountModel(BlueAccountDto dto) {
    super(dto);
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
