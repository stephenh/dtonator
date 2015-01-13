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
    final EmployeeAccountDto b = new EmployeeAccountDto(a);
    assertThat(b.id, is(a.id));
    assertThat(b.name, is(a.name));
    a.name = "bar";
    assertThat(b.name, is("foo"));
  }

  @Test
  public void testCopyConstructorWithChildren() {
    final EmployeeWithAccountsDto a = new EmployeeWithAccountsDto(1L, "bob", list(new EmployeeAccountDto(2L, new Dollars(250), "a1")));
    final EmployeeWithAccountsDto b = new EmployeeWithAccountsDto(a);
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
    final EmployeeWithTypedAccountsDto b = new EmployeeWithTypedAccountsDto(a);
    assertThat(b.id, is(a.id));
    assertThat(b.name, is(a.name));
    a.name = "bar";
    assertThat(b.name, is("bob"));
    a.accounts.get(0).name = "a2";
    assertThat(b.accounts.get(0).name, is("a1"));
  }

}
