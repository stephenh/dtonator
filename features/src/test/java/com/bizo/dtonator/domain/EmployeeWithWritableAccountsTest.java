package com.bizo.dtonator.domain;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeAccountDto;
import com.bizo.dtonator.dtos.EmployeeWithWritableAccountsDto;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeWithWritableAccountsTest {

  private final StubDomainLookup lookup = new StubDomainLookup();
  private final Mapper mapper = new Mapper(lookup, null, null, null);

  @Test
  public void testToDto() {
    final Employee e1 = new Employee(1l, "e1");
    e1.setAccounts(newArrayList(new EmployeeAccount(2l, "ea1")));
    final EmployeeWithWritableAccountsDto dto = mapper.toEmployeeWithWritableAccountsDto(e1);
    assertThat(dto.accounts.size(), is(1));
    assertThat(dto.accounts.get(0), is(instanceOf(EmployeeAccountDto.class)));
  }

  @Test
  public void testFromDto() {
    // store a child account that our incoming dto will refer to by id
    final EmployeeAccount ea1 = new EmployeeAccount(2l, "ea1");
    lookup.store(2l, ea1);

    // incoming employee with an account
    final EmployeeWithWritableAccountsDto dto = new EmployeeWithWritableAccountsDto(null, "e1", newArrayList( //
      new EmployeeAccountDto(2l, null, "changed name")));

    final Employee ee = mapper.fromDto(dto);
    assertThat(ee.getAccounts().size(), is(1));
    // the same ea1 instance from lookup was used
    assertThat(ee.getAccounts(), contains(ea1));
    // and we did recursively write back the name
    assertThat(ea1.getName(), is("changed name"));
  }

  @Test
  public void testFromDtoWithANewChild() {
    // incoming employee with a new account
    final EmployeeWithWritableAccountsDto dto = new EmployeeWithWritableAccountsDto(null, "e1", newArrayList( //
      new EmployeeAccountDto(null, null, "changed")));

    final Employee ee = mapper.fromDto(dto);
    assertThat(ee.getAccounts().size(), is(1));
    // and we did recursively write back the name
    assertThat(ee.getAccounts().get(0).getName(), is("changed"));
  }

}