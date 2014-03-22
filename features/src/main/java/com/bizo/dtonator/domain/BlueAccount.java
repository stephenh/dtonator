package com.bizo.dtonator.domain;

public class BlueAccount extends Account {

  private boolean bar;

  public BlueAccount() {
  }

  public BlueAccount(Long id, boolean bar) {
    super(id);
    this.bar = bar;
  }

  public boolean isBar() {
    return bar;
  }

  public void setBar(boolean bar) {
    this.bar = bar;
  }
}
