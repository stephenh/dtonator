package com.bizo.dtonator.domain;

public class Employee {

  private Long id;
  private String name;
  private boolean working;
  private EmployeeType type;

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public boolean isWorking() {
    return working;
  }

  public void setWorking(final boolean working) {
    this.working = working;
  }

  public EmployeeType getType() {
    return type;
  }

  public void setType(final EmployeeType type) {
    this.type = type;
  }

}
