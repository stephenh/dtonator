package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeDto;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeTest {

  private final Mapper mapper = new Mapper();

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
    final EmployeeDto dto = new EmployeeDto(1l, "e", com.bizo.dtonator.dtos.EmployeeType.LARGE, true);

    final Employee e = new Employee();
    mapper.fromDto(e, dto);
    assertThat(e.getId(), is(1l));
    assertThat(e.getName(), is("e"));
    assertThat(e.isWorking(), is(true));
    assertThat(e.getType(), is(com.bizo.dtonator.domain.EmployeeType.LARGE));
  }
}
