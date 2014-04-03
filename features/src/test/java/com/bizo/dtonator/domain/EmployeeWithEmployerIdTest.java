package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeWithEmployerIdDto;
import com.bizo.dtonator.mapper.DefaultDollarsMapper;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeWithEmployerIdTest {

  private final StubDomainLookup lookup = new StubDomainLookup();
  private final Mapper mapper = new Mapper(lookup, null, null, null, null, new DefaultDollarsMapper());

  @Test
  public void testToDtoWithNullEmployer() {
    final Employee e = new Employee(1l, "ee");

    final EmployeeWithEmployerIdDto dto = mapper.toEmployeeWithEmployerIdDto(e);
    assertThat(dto.id, is(1l));
    assertThat(dto.employerId, is(nullValue()));
  }

  @Test
  public void testToDtoWithEmployer() {
    final Employee ee = new Employee(1l, "ee");
    final Employer er = new Employer(2l, "er");
    ee.setEmployer(er);

    final EmployeeWithEmployerIdDto dto = mapper.toEmployeeWithEmployerIdDto(ee);
    assertThat(dto.id, is(1l));
    assertThat(dto.employerId, is(2l));
  }

  @Test
  public void testFromDtoWithNullId() {
    final EmployeeWithEmployerIdDto dto = new EmployeeWithEmployerIdDto(null, null);
    final Employee ee = mapper.fromDto(dto);
    assertThat(ee.getEmployer(), is(nullValue()));
  }

  @Test
  public void testFromDtoWithSetId() {
    final Employer er = new Employer(2l, "er");
    lookup.store(2l, er);
    final EmployeeWithEmployerIdDto dto = new EmployeeWithEmployerIdDto(null, 2l);
    final Employee ee = mapper.fromDto(dto);
    assertThat(ee.getEmployer(), is(er));
  }

}
