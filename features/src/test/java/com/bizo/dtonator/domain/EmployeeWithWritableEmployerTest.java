package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeWithWritableEmployerDto;
import com.bizo.dtonator.dtos.EmployerDto;
import com.bizo.dtonator.mapper.DefaultDollarsMapper;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeWithWritableEmployerTest {

  private final StubDomainLookup lookup = new StubDomainLookup();
  private final Mapper mapper = new Mapper(lookup, null, null, new DefaultDollarsMapper());

  @Test
  public void testToDto() {
    final Employee e = new Employee(1l, "e");
    e.setEmployer(new Employer(1l, "er"));

    final EmployeeWithWritableEmployerDto dto = mapper.toEmployeeWithWritableEmployerDto(e);
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
    final EmployeeWithWritableEmployerDto dto =
      new EmployeeWithWritableEmployerDto(null, "e", new EmployerDto(1l, "changed"));

    final Employee e = mapper.fromDto(dto);
    assertThat(e.getName(), is("e"));
    assertThat(e.getEmployer(), is(sameInstance(er1)));
    // since this is recursive, write back to the employer
    assertThat(e.getEmployer().getName(), is("changed"));
  }

  @Test
  public void testFromDtoWithANewEmployer() {
    // incoming employee with the er id null
    final EmployeeWithWritableEmployerDto dto =
      new EmployeeWithWritableEmployerDto(null, "e", new EmployerDto(null, "er"));

    final Employee e = mapper.fromDto(dto);
    assertThat(e.getName(), is("e"));
    // we go ahead and instantiate a new employer...
    assertThat(e.getEmployer(), is(not(nullValue())));
    // ...and also write back the employer's attributes
    assertThat(e.getEmployer().getName(), is("er"));
  }
}
