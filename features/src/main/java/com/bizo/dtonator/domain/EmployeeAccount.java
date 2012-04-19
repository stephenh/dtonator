package com.bizo.dtonator.domain;

public class EmployeeAccount {

  private Long id;
  private String name;
  private Dollars balance;

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

  public Dollars getBalance() {
    return balance;
  }

  public void setBalance(final Dollars balance) {
    this.balance = balance;
  }

}
