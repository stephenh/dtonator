package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeWithOnlyNameDto;
import com.bizo.dtonator.mapper.DefaultDollarsMapper;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeWithOnlyNameTest {

  private final StubDomainLookup lookup = new StubDomainLookup();
  private final Mapper mapper = new Mapper(lookup, null, null, null, new DefaultDollarsMapper());

  @Test
  public void testToDto() {
    final Employee e = new Employee(1l, "ee");
    final EmployeeWithOnlyNameDto dto = mapper.toEmployeeWithOnlyNameDto(e);
    assertThat(dto.name, is("ee"));
  }

  @Test
  public void testFromDto() {
    final EmployeeWithOnlyNameDto dto = new EmployeeWithOnlyNameDto("ee");
    final Employee e = new Employee();
    mapper.fromDto(e, dto);
    assertThat(e.getName(), is("ee"));
  }

}
