package com.bizo.dtonator.properties;

/** A simple DTO to abstract discovering properties via reflection. */
public class Prop {

  public final String name;
  public final String type;
  public final boolean readOnly;
  public final String getterMethodName;
  public final String setterNameMethod;

  public Prop(
      final String name,
      final String type,
      final boolean readOnly,
      final String getterMethodName,
      final String setterNameMethod) {
    super();
    this.name = name;
    this.type = type;
    this.readOnly = readOnly;
    this.getterMethodName = getterMethodName;
    this.setterNameMethod = setterNameMethod;
  }

  @Override
  public String toString() {
    return name + " " + type;
  }
}
