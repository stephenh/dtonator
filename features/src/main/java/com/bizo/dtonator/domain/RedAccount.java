package com.bizo.dtonator.domain;

public class RedAccount extends Account {

  private boolean foo;

  public RedAccount() {
  }

  public RedAccount(Long id, String name, boolean foo) {
    super(id, name);
    this.foo = foo;
  }

  public boolean isFoo() {
    return foo;
  }

  public void setFoo(boolean foo) {
    this.foo = foo;
  }

}
