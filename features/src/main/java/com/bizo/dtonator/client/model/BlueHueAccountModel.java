package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.BlueHueAccountDto;

public class BlueHueAccountModel extends BlueHueAccountModelCodegen {

  public BlueHueAccountModel(BlueHueAccountDto dto) {
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
