package com.bizo.dtonator.domain;

public class GreenHueAccount extends GreenAccount {

  public Integer hue;

  public GreenHueAccount(Long id, String name, boolean foo, String oof, Integer hue) {
    super(id, name, foo, oof);
    this.hue = hue;
  }

  public Integer getHue() {
    return hue;
  }

  public void setHue(Integer hue) {
    this.hue = hue;
  }

}
