package com.bizo.dtonator.domain;

import java.util.List;

public class Employee {

  private Long id;
  private String name;
  private boolean working;
  private EmployeeType type;
  private Dollars salary;
  private List<EmployeeAccount> accounts;

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

  public Dollars getSalary() {
    return salary;
  }

  public void setSalary(final Dollars salary) {
    this.salary = salary;
  }

  public List<EmployeeAccount> getAccounts() {
    return accounts;
  }

  public void setAccounts(final List<EmployeeAccount> accounts) {
    this.accounts = accounts;
  }

}
