package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.dtos.EmployeeExtensionDto;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeExtensionDtoTest {

  private final StubEmployeeExtensionMapper employeeExtMapper = new StubEmployeeExtensionMapper();
  private final Mapper mapper = new Mapper(employeeExtMapper);

  @Test
  public void testToDto() {
    final Employee e = new Employee();
    e.setId(1l);

    final EmployeeExtensionDto dto = mapper.toEmployeeExtensionDto(e);
    assertThat(dto.id, is(1l));
    // hard coded to 1
    assertThat(dto.extensionValue, is(1));
  }

  @Test
  public void testFromDto() {
    final EmployeeExtensionDto dto = new EmployeeExtensionDto(1l, 2);

    final Employee e = new Employee();
    mapper.fromDto(e, dto);
    assertThat(e.getId(), is(1l));
    assertThat(employeeExtMapper.extensionValues, contains(2));
  }
}
