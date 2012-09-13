package com.bizo.dtonator.dtos;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class EqualsTest {

  @Test
  public void testCstr() {
    final ClientOnlyDto d = new ClientOnlyDto(1l, "asdf");
    assertThat(d.id, is(1l));
    assertThat(d.name, is("asdf"));
  }

  @Test
  public void testEquals() {
    final ClientOnlyDto d1 = new ClientOnlyDto(1l, "asdf1");
    final ClientOnlyDto d2 = new ClientOnlyDto(1l, "asdf2");
    assertThat(d1.equals(d2), is(true));
    assertThat(d1.hashCode() == d2.hashCode(), is(true));
  }

  @Test
  public void testEqualsWithStar() {
    final EmployeeDto e1 = new EmployeeDto(1l, "e1", new Dollars(1), EmployeeType.LARGE, true);
    final EmployeeDto e2 = new EmployeeDto(1l, "e1", new Dollars(1), EmployeeType.LARGE, true);
    assertThat(e1.equals(e2), is(true));
    assertThat(e1.hashCode() == e2.hashCode(), is(true));
  }

  @Test
  public void testToStringWithEqualitySet() {
    // equality only had equals, so we use it for toString as well
    final ClientOnlyDto d1 = new ClientOnlyDto(1l, "asdf1");
    assertThat(d1.toString(), is("ClientOnlyDto[1]"));
  }

  @Test
  public void testToStringWithEqualitySetToStar() {
    // equality had *, so do everything
    final EmployeeDto e1 = new EmployeeDto(1l, "e1", new Dollars(1), EmployeeType.LARGE, true);
    assertThat(e1.toString(), is("EmployeeDto[1, e1, 1, LARGE, true]"));
  }

  @Test
  public void testToStringWithNoEqualitySet() {
    final EmployerDto er = new EmployerDto(1l, "er");
    assertThat(er.toString(), is("EmployerDto[1, er]"));
  }

}
