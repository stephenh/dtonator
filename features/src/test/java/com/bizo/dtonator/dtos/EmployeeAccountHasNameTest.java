package com.bizo.dtonator.dtos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EmployeeAccountHasNameTest {

  @Test
  public void test() {
    final HasName n = new EmployeeAccountHasName("name");
    assertThat(n.getName(), is("name"));
  }

}
