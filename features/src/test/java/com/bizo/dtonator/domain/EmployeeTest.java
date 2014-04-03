package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.dtos.Dollars;
import com.bizo.dtonator.dtos.EmployeeDto;
import com.bizo.dtonator.mapper.DefaultDollarsMapper;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeTest {

  private final StubDomainLookup lookup = new StubDomainLookup();
  private final Mapper mapper = new Mapper(lookup, null, null, null, null, new DefaultDollarsMapper());

  @Test
  public void testToDto() {
    final Employee e = new Employee();
    e.setId(1l);
    e.setName("e");
    e.setWorking(true);
    e.setType(com.bizo.dtonator.domain.EmployeeType.LARGE);

    final EmployeeDto dto = mapper.toDto(e);
    assertThat(dto.id, is(1l));
    assertThat(dto.name, is("e"));
    assertThat(dto.working, is(true));
    assertThat(dto.type, is(com.bizo.dtonator.dtos.EmployeeType.LARGE));
  }

  @Test
  public void testFromDto() {
    final EmployeeDto dto = new EmployeeDto(1l, "e", new Dollars(100), com.bizo.dtonator.dtos.EmployeeType.LARGE, true);

    final Employee e = new Employee();
    mapper.fromDto(e, dto);
    // the id should have been set when looking up the Employee...don't overwrite it
    assertThat(e.getId(), is(nullValue()));
    assertThat(e.getName(), is("e"));
    assertThat(e.isWorking(), is(true));
    assertThat(e.getType(), is(com.bizo.dtonator.domain.EmployeeType.LARGE));
  }

  @Test
  public void testFromDtoWithLookupExisting() {
    // given an existing employee
    final Employee e = new Employee();
    e.setId(1l);
    lookup.store(1l, e);

    // when a dto comes in with id == 1
    final EmployeeDto dto = new EmployeeDto(1l, "e", new Dollars(100), com.bizo.dtonator.dtos.EmployeeType.LARGE, true);
    final Employee e2 = mapper.fromDto(dto);

    // we have the same instance
    assertThat(e2, is(sameInstance(e)));
    // and it got updated
    assertThat(e2.getName(), is("e"));
  }

  @Test
  public void testFromDtoWithLookupButNewInstance() {
    // when a dto comes in with id == null
    final EmployeeDto dto = new EmployeeDto(null, "e", new Dollars(100), com.bizo.dtonator.dtos.EmployeeType.LARGE, true);
    // then we get a new instance
    final Employee e = mapper.fromDto(dto);
    // and it got mapped
    assertThat(e.getId(), is(nullValue()));
    assertThat(e.getName(), is("e"));
  }

  @Test
  public void testDollarsValueTypeToDto() {
    final Employee e = new Employee();
    e.setSalary(new com.bizo.dtonator.domain.Dollars(100));
    final EmployeeDto dto = mapper.toDto(e);
    assertThat(dto.salary.cents, is(100));
  }

  @Test
  public void testDollarsValueTypeFromDto() {
    final EmployeeDto dto = new EmployeeDto(null, "e", new Dollars(100), null, true);
    final Employee e = mapper.fromDto(dto);
    assertThat(e.getSalary().cents, is(100));
  }
}
