package com.bizo.dtonator.domain;

import java.util.ArrayList;
import java.util.List;

public class EmployeeWithTypedAccounts {

  private Long id;
  private String name;
  private List<Account> accounts = new ArrayList<Account>();

  public EmployeeWithTypedAccounts() {
  }

  public EmployeeWithTypedAccounts(final Long id, final String name) {
    setId(id);
    setName(name);
  }

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

  public List<Account> getAccounts() {
    return accounts;
  }

  public void setAccounts(final List<Account> accounts) {
    this.accounts = accounts;
  }

}
