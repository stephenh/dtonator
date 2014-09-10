package com.bizo.dtonator.domain;

public class BlueHueAccount extends BlueAccount {

  private boolean zaz;

  public BlueHueAccount() {
  }

  public BlueHueAccount(Long id, String name, boolean bar, boolean zaz) {
    super(id, name, bar);
    this.zaz = zaz;
  }

  public boolean isZaz() {
    return zaz;
  }

  public void setZaz(boolean zaz) {
    this.zaz = zaz;
  }

}
