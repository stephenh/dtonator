package com.bizo.dtonator.properties;

public class Prop {

  public final String name;
  public final String type;
  public final String getterMethodName;
  public final String setterNameMethod;

  public Prop(final String name, final String type, final String getterMethodName, final String setterNameMethod) {
    super();
    this.name = name;
    this.type = type;
    this.getterMethodName = getterMethodName;
    this.setterNameMethod = setterNameMethod;
  }

  @Override
  public String toString() {
    return name + " " + type;
  }
}
