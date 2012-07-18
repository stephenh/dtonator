package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeWithEmployerDto;
import com.bizo.dtonator.dtos.EmployerDto;
import com.bizo.dtonator.mapper.DefaultDollarsMapper;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeWithEmployerTest {

  private final StubDomainLookup lookup = new StubDomainLookup();
  private final Mapper mapper = new Mapper(lookup, null, null, new DefaultDollarsMapper());

  @Test
  public void testToDto() {
    final Employee e = new Employee(1l, "e");
    e.setEmployer(new Employer(1l, "er"));

    final EmployeeWithEmployerDto dto = mapper.toEmployeeWithEmployerDto(e);
    assertThat(dto.id, is(1l));
    assertThat(dto.name, is("e"));
    assertThat(dto.employer.id, is(1l));
    assertThat(dto.employer.name, is("er"));
  }

  @Test
  public void testFromDto() {
    // store a reference to er1 for the incoming dto to refer to
    final Employer er1 = new Employer(1l, "er");
    lookup.store(1l, er1);

    // incoming employee with the er set
    final EmployeeWithEmployerDto dto = new EmployeeWithEmployerDto(null, "e", new EmployerDto(1l, "er changed"));

    final Employee e = mapper.fromDto(dto);
    assertThat(e.getName(), is("e"));
    assertThat(e.getEmployer(), is(sameInstance(er1)));
    // by default don't write over the employer's attributes
    assertThat(e.getEmployer().getName(), is("er"));
  }

  @Test
  public void testFromDtoWithANewEmployer() {
    // incoming employee with the er id null
    final EmployeeWithEmployerDto dto = new EmployeeWithEmployerDto(null, "e", new EmployerDto(null, "er"));

    final Employee e = mapper.fromDto(dto);
    assertThat(e.getName(), is("e"));
    // we go ahead and instantiate a new employer...
    assertThat(e.getEmployer(), is(not(nullValue())));
    // ...but don't write over the employer's attributes
    assertThat(e.getEmployer().getName(), is(nullValue()));
  }

  @Test
  public void testFromDtoWithANullEmployer() {
    // incoming employee with null er
    final EmployeeWithEmployerDto dto = new EmployeeWithEmployerDto(null, "e", null);

    final Employee e = mapper.fromDto(dto);
    assertThat(e.getName(), is("e"));
    assertThat(e.getEmployer(), is(nullValue()));
  }
}
