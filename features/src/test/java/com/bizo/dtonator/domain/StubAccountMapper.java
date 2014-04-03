package com.bizo.dtonator.domain;

import com.bizo.dtonator.mapper.AccountDtoMapper;
import com.bizo.dtonator.mapper.Mapper;

public class StubAccountMapper implements AccountDtoMapper {

  private String suffix;

  public StubAccountMapper() {
    this("");
  }

  public StubAccountMapper(String suffix) {
    this.suffix = suffix;
  }

  @Override
  public String getName(Mapper m, Account account) {
    return account.getName() + suffix;
  }

  @Override
  public void setName(Mapper m, Account account, String name) {
    account.setName(name + suffix);
  }

}
