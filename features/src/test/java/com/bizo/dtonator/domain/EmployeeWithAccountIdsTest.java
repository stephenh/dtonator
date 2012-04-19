package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeWithAccountIdsDto;

public class EmployeeWithAccountIdsTest {

  @Test
  public void testDefaultConstructor() {
    final EmployeeWithAccountIdsDto e = new EmployeeWithAccountIdsDto();
    assertThat(e.name, is(nullValue()));
    assertThat(e.accounts.size(), is(0));
  }

}