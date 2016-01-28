package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.Test;
import org.tessell.model.properties.EnumProperty;

import com.bizo.dtonator.client.model.EmployeeWithTypeModel;
import com.bizo.dtonator.dtos.EmployeeType;
import com.bizo.dtonator.dtos.EmployeeWithTypeDto;

public class EmployeeWithTypeDtoTest {

  @Test
  public void test() {
    final EmployeeWithTypeModel m = new EmployeeWithTypeModel(new EmployeeWithTypeDto("asdf", EmployeeType.SMALL));
    assertThat(m.type, is(instanceOf(EnumProperty.class)));
  }

  @Test
  public void testGetters() {
    final EmployeeWithTypeModel m = new EmployeeWithTypeModel(new EmployeeWithTypeDto("asdf", EmployeeType.SMALL));
    assertThat(m.type.get().getDisplayText(), is("Small"));
  }
}
