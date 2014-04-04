package com.bizo.dtonator.client.model;

import static joist.util.Copy.list;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;
import org.tessell.model.validation.rules.Required;

import com.bizo.dtonator.dtos.Dollars;
import com.bizo.dtonator.dtos.EmployeeAccountTslDto;
import com.bizo.dtonator.dtos.EmployeeWithAccountsTslDto;

public class EmployeeWithAccountsTslModelTest {

  final EmployeeWithAccountsTslModel e = new EmployeeWithAccountsTslModel(new EmployeeWithAccountsTslDto(//
    "e",
    new ArrayList<EmployeeAccountTslDto>(),
    new ArrayList<EmployeeAccountTslDto>()));

  @Test
  public void hasAccountModels() {
    assertThat(e.accountModels.get().size(), is(0));
    assertThat(e.otherAccountModels.get().size(), is(0));
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
    assertThat(e.accountModels.get().get(0).allValid().get(), is(false));
    assertThat(e.accountModels.get().get(0).allValid().isTouched(), is(true));
    assertThat(e.allValid().isTouched(), is(true));
    assertThat(e.allValid().get(), is(false));
  }

  @Test
  public void findsExistingChildrenInDto() {
    final EmployeeAccountTslDto c1 = new EmployeeAccountTslDto(1l, new Dollars(0), null);
    final EmployeeWithAccountsTslModel e = new EmployeeWithAccountsTslModel(//
      new EmployeeWithAccountsTslDto("e", list(c1), list(c1)));
    assertThat(e.accounts.get().size(), is(1));
    assertThat(e.accountModels.get().size(), is(1));
    assertThat(e.otherAccounts.get().size(), is(1));
    assertThat(e.otherAccountModels.get().size(), is(1));
  }

  @Test
  public void invalidListOfDtosMeansInvalidParent() {
    e.accounts.addRule(new Required());
    e.accounts.touch();
    assertThat(e.allValid().get(), is(false));
  }

  @Test
  public void invalidListOfModelsMeansInvalidParent() {
    e.accountModels.addRule(new Required());
    e.accountModels.touch();
    assertThat(e.allValid().get(), is(false));
  }
}
