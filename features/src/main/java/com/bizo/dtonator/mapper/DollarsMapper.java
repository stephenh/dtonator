package com.bizo.dtonator.mapper;

import com.bizo.dtonator.dtos.Dollars;

public class DollarsMapper extends AbstractDollarsMapper {

  @Override
  public Dollars toDto(final com.bizo.dtonator.domain.Dollars dollars) {
    return new Dollars(dollars.cents);
  }

  @Override
  public com.bizo.dtonator.domain.Dollars fromDto(final Dollars dollars) {
    return new com.bizo.dtonator.domain.Dollars(dollars.cents);
  }

}
