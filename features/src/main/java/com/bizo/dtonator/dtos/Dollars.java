package com.bizo.dtonator.dtos;

public class Dollars {

  public final int cents;

  public Dollars(final int cents) {
    this.cents = cents;
  }

  public int getCents() {
    return cents;
  }

  @Override
  public boolean equals(final Object other) {
    return other instanceof Dollars && ((Dollars) other).cents == cents;
  }

  @Override
  public int hashCode() {
    return cents; // yeah this is dumb
  }

  @Override
  public String toString() {
    return Integer.toString(cents);
  }

}
