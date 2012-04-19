package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeWithAccountDto;

public class EmployeeWithAccountsTest {

  @Test
  public void testDefaultConstructor() {
    final EmployeeWithAccountDto e = new EmployeeWithAccountDto();
    assertThat(e.name, is(nullValue()));
    assertThat(e.accounts.size(), is(0));
  }

}