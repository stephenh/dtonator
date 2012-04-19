package com.bizo.dtonator.domain;

// Exactly like the dto type, but the idea is that they would be different.
public class Dollars {

  public final int cents;

  public Dollars(final int cents) {
    this.cents = cents;
  }

  public int getCents() {
    return cents;
  }

}
