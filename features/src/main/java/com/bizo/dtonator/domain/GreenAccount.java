package com.bizo.dtonator.domain;

public class GreenAccount extends Account {

  private boolean baz;
  private String oof;

  public GreenAccount() {
  }

  public GreenAccount(Long id, String name, boolean foo, String oof) {
    super(id, name);
    this.baz = foo;
    this.oof = oof;
  }

  public boolean isBaz() {
    return baz;
  }

  public void setBaz(boolean baz) {
    this.baz = baz;
  }

  public String getOof() {
    return oof;
  }

  public void setOof(String oof) {
    this.oof = oof;
  }

}
