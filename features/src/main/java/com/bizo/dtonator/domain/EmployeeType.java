package com.bizo.dtonator.domain;

public enum EmployeeType {
  LARGE("Large"), SMALL("Small");

  private final String displayText;

  private EmployeeType(final String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }
}
