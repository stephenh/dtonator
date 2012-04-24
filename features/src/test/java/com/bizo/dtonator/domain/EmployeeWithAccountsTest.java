package com.bizo.dtonator.domain;

import static joist.util.Copy.list;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeAccountDto;
import com.bizo.dtonator.dtos.EmployeeWithAccountsDto;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeWithAccountsTest {

  private final StubDomainLookup lookup = new StubDomainLookup();
  private final Mapper mapper = new Mapper(lookup, null, null, null);

  @Test
  public void testToDto() {
    final Employee e1 = new Employee(1l, "e1");
    e1.setAccounts(list(new EmployeeAccount(2l, "ea1")));
    final EmployeeWithAccountsDto dto = mapper.toEmployeeWithAccountsDto(e1);
    assertThat(dto.accounts.size(), is(1));
    assertThat(dto.accounts.get(0).id, is(2l));
    assertThat(dto.accounts.get(0).name, is("ea1"));
  }

  @Test
  public void testFromDto() {
    // store a child account that our incoming dto will refer to by id
    final EmployeeAccount ea1 = new EmployeeAccount(2l, "ea1");
    lookup.store(2l, ea1);

    // incoming employee with an account
    final EmployeeWithAccountsDto dto = new EmployeeWithAccountsDto(null, "e1", list( //
      new EmployeeAccountDto(2l, null, "changed name")));

    final Employee ee = mapper.fromDto(dto);
    assertThat(ee.getAccounts().size(), is(1));
    // the same ea1 instance from lookup was used
    assertThat(ee.getAccounts(), contains(ea1));
    // and we didn't override the name (for now/in this configuration)
    assertThat(ea1.getName(), is("ea1"));
  }

  @Test
  public void testFromDtoWithANewChild() {
    // incoming employee with a new account
    final EmployeeWithAccountsDto dto = new EmployeeWithAccountsDto(null, "e1", list( //
      new EmployeeAccountDto(null, null, "name")));
    final Employee ee = mapper.fromDto(dto);
    // since recursive writes are disabled, we created an instance
    assertThat(ee.getAccounts().size(), is(1));
    // but it's not populated
    assertThat(ee.getAccounts().get(0).getName(), is(nullValue()));
  }
}
