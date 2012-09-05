package com.bizo.dtonator.domain;

public class EmployeeWithFooId {

  private Long id;
  private String foo;
  private String fooId;

  public EmployeeWithFooId() {
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getFoo() {
    return foo;
  }

  public void setFoo(final String foo) {
    this.foo = foo;
  }

  public String getFooId() {
    return fooId;
  }

  public void setFooId(final String fooId) {
    this.fooId = fooId;
  }

}
