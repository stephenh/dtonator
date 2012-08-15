package com.bizo.dtonator.domain;

import static joist.util.Copy.list;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeSimpleAccountDto;
import com.bizo.dtonator.dtos.EmployeeWithSimpleAccountsDto;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeWithSimpleAccountsTest {

  private final StubDomainLookup lookup = new StubDomainLookup();
  private final Mapper mapper = new Mapper(lookup, null, null, null, null);

  @Test
  public void testToDto() {
    final Employee e1 = new Employee(1l, "e1");
    e1.setAccounts(list(new EmployeeAccount(2l, "ea1")));
    final EmployeeWithSimpleAccountsDto dto = mapper.toEmployeeWithSimpleAccountsDto(e1);
    assertThat(dto.accounts.size(), is(1));
    assertThat(dto.accounts.get(0), is(instanceOf(EmployeeSimpleAccountDto.class)));
  }

  @Test
  public void testFromDto() {
    // store a child account that our incoming dto will refer to by id
    final EmployeeAccount ea1 = new EmployeeAccount(2l, "ea1");
    lookup.store(2l, ea1);

    // incoming employee with an account
    final EmployeeWithSimpleAccountsDto dto = new EmployeeWithSimpleAccountsDto(null, "e1", list( //
      new EmployeeSimpleAccountDto(2l, "changed name")));

    final Employee ee = mapper.fromDto(dto);
    assertThat(ee.getAccounts().size(), is(1));
    // the same ea1 instance from lookup was used
    assertThat(ee.getAccounts(), contains(ea1));
    // and we wrote back the new name
    assertThat(ea1.getName(), is("changed name"));
  }

}
