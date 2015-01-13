package com.bizo.dtonator.dtos;

import static joist.util.Copy.list;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import joist.util.Copy;

import org.junit.Test;

public class EmployeeAccountDtoTest {

  @Test
  public void testCopyConstructor() {
    final EmployeeAccountDto a = new EmployeeAccountDto(1L, new Dollars(250), "foo");
    final EmployeeAccountDto b = a.copy();
    assertThat(b.id, is(a.id));
    assertThat(b.name, is(a.name));
    a.name = "bar";
    assertThat(b.name, is("foo"));
  }

  @Test
  public void testCopyConstructorWithChildren() {
    final EmployeeWithAccountsDto a = new EmployeeWithAccountsDto(1L, "bob", list(new EmployeeAccountDto(2L, new Dollars(250), "a1")));
    final EmployeeWithAccountsDto b = a.copy();
    assertThat(b.id, is(a.id));
    assertThat(b.name, is(a.name));
    a.name = "bar";
    assertThat(b.name, is("bob"));
    a.accounts.get(0).name = "a2";
    assertThat(b.accounts.get(0).name, is("a1"));
  }

  @Test
  public void testCopyConstructorWithChildrenThatHaveSubClasses() {
    final EmployeeWithTypedAccountsDto a = new EmployeeWithTypedAccountsDto(//
      1L,
      "bob",
      Copy.<AccountDto> list(new BlueHueAccountDto(2L, "a1", true, true)));
    final EmployeeWithTypedAccountsDto b = a.copy();
    assertThat(b.id, is(a.id));
    assertThat(b.name, is(a.name));
    a.name = "bar";
    assertThat(b.name, is("bob"));
    a.accounts.get(0).name = "a2";
    assertThat(b.accounts.get(0).name, is("a1"));
  }

  @Test
  public void testCopyConstructorWithParentDto() {
    final EmployeeWithEmployerDto a = new EmployeeWithEmployerDto(1L, "bob", new EmployerDto(2L, "er1"));
    final EmployeeWithEmployerDto b = a.copy();
    assertThat(b.id, is(a.id));
    assertThat(b.name, is(a.name));
    a.name = "bar";
    assertThat(b.name, is("bob"));
    a.employer.name = "er2";
    assertThat(b.employer.name, is("er1"));
  }

}
