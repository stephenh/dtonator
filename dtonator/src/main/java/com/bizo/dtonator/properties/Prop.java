package com.bizo.dtonator.properties;

/** A simple DTO to abstract discovering properties via reflection. */
public class Prop {

  public final String name;
  public final String type;
  public final boolean readOnly;
  private final String getterMethodName;
  private final String setterNameMethod;
  private final String getterMethodNameDeclaredIn;
  private final String setterNameMethodDeclaredIn;

  public Prop(
    String name,
    String type,
    boolean readOnly,
    String getterMethodName,
    String setterNameMethod,
    String getterMethodNameDeclaredIn,
    String setterNameMethodDeclaredIn) {
    super();
    this.name = name;
    this.type = type;
    this.readOnly = readOnly;
    this.getterMethodName = getterMethodName;
    this.setterNameMethod = setterNameMethod;
    this.getterMethodNameDeclaredIn = getterMethodNameDeclaredIn;
    this.setterNameMethodDeclaredIn = setterNameMethodDeclaredIn;
  }

  public String getGetterMethodName() {
    return getterMethodName;
  }

  public String getSetterNameMethod() {
    return setterNameMethod;
  }

  public String getGetterMethodNameDeclaredIn() {
    return getterMethodNameDeclaredIn;
  }

  public String getSetterNameMethodDeclaredIn() {
    return setterNameMethodDeclaredIn;
  }

  @Override
  public String toString() {
    return name + " " + type;
  }
}
