package com.bizo.dtonator.domain;

public enum EmployeeType {
  LARGE("Large", 10), SMALL("Small", 5);

  private final String displayText;
  private final Integer size;

  private EmployeeType(final String displayText, final Integer size) {
    this.displayText = displayText;
    this.size = size;
  }

  public String getDisplayText() {
    return displayText;
  }

  public Integer getSize() {
    return size;
  }
}
