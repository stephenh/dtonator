package com.bizo.dtonator.client.model;

import static joist.util.Copy.list;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import com.bizo.dtonator.dtos.Dollars;
import com.bizo.dtonator.dtos.EmployeeAccountTslDto;
import com.bizo.dtonator.dtos.EmployeeWithAccountsTslDto;

public class EmployeeWithAccountsTslModelTest {

  final EmployeeWithAccountsTslModel e = new EmployeeWithAccountsTslModel(//
    new EmployeeWithAccountsTslDto("e", new ArrayList<EmployeeAccountTslDto>()));

  @Test
  public void hasAccountModels() {
    assertThat(e.accountModels.get().size(), is(0));
  }

  @Test
  public void addingDtoAddsModel() {
    e.accounts.add(new EmployeeAccountTslDto(1l, new Dollars(0), "a"));
    assertThat(e.accountModels.get().size(), is(1));
    assertThat(e.accountModels.get().get(0).name.get(), is("a"));
  }

  @Test
  public void invalidChildMeansAnInvalidParent() {
    // name is required
    final EmployeeAccountTslDto c1 = new EmployeeAccountTslDto(1l, new Dollars(0), null);
    e.accounts.add(c1);
    e.accountModels.get().get(0).name.setTouched(true);
    assertThat(e.accountModels.get().get(0).all().get(), is(false));
    assertThat(e.accountModels.get().get(0).all().isTouched(), is(true));
    assertThat(e.all().isTouched(), is(true));
    assertThat(e.all().get(), is(false));
  }

  @Test
  public void findsExistingChildrenInDto() {
    final EmployeeAccountTslDto c1 = new EmployeeAccountTslDto(1l, new Dollars(0), null);
    final EmployeeWithAccountsTslModel e = new EmployeeWithAccountsTslModel(//
      new EmployeeWithAccountsTslDto("e", list(c1)));
    assertThat(e.accounts.get().size(), is(1));
    assertThat(e.accountModels.get().size(), is(1));
  }
}
