package com.bizo.dtonator.domain;

public class Employer {

  private Long id;
  private String name;

  public Employer() {
  }

  public Employer(final Long id, final String name) {
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

}
