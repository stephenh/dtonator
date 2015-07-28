package com.bizo.dtonator.client.model;

import com.bizo.dtonator.dtos.AccountDto;

public abstract class AccountModel extends AccountModelCodegen {

  public AccountModel(AccountDto dto) {
    super(dto);
    addRules();
    merge(dto);
  }

  private void addRules() {
  }

}
