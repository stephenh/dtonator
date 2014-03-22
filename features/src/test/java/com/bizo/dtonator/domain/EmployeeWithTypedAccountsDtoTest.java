package com.bizo.dtonator.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import java.util.ArrayList;

import org.junit.Test;

import com.bizo.dtonator.dtos.AccountDto;
import com.bizo.dtonator.dtos.BlueAccountDto;
import com.bizo.dtonator.dtos.EmployeeWithTypedAccountsDto;
import com.bizo.dtonator.dtos.RedAccountDto;
import com.bizo.dtonator.mapper.DefaultDollarsMapper;
import com.bizo.dtonator.mapper.Mapper;

public class EmployeeWithTypedAccountsDtoTest {

  private final StubDomainLookup lookup = new StubDomainLookup();
  private final Mapper mapper = new Mapper(lookup, null, null, null, new DefaultDollarsMapper());

  @Test
  public void testChildDtoExtendsParentDto() {
    RedAccountDto red = new RedAccountDto(1L, true);
    assertThat(red, is(instanceOf(AccountDto.class)));
  }

  @Test
  public void testChildDtoToString() {
    RedAccountDto red = new RedAccountDto(1L, true);
    assertThat(red.toString(), is("RedAccountDto[1, true]"));
  }

  @Test
  public void testChildDtoFullConstructorSetsValuesInBaseClass() {
    RedAccountDto red = new RedAccountDto(1L, true);
    assertThat(red.id, is(1L));
  }

  @Test
  public void testToDtoViaSubClassMethod() {
    RedAccount o = new RedAccount(1L, true);
    RedAccountDto d = mapper.toRedAccountDto(o);
    assertThat(d.id, is(1L));
    assertThat(d.foo, is(true));
  }

  @Test
  public void testToDtoViaBaseClassMethod() {
    RedAccount o = new RedAccount(1L, true);
    RedAccountDto d = (RedAccountDto) mapper.toAccountDto(o);
    assertThat(d.id, is(1L));
    assertThat(d.foo, is(true));
  }

  @Test
  public void testParentToDto() {
    EmployeeWithTypedAccounts e = new EmployeeWithTypedAccounts(1L, "name");
    e.getAccounts().add(new RedAccount(2L, true));
    e.getAccounts().add(new BlueAccount(3L, true));

    EmployeeWithTypedAccountsDto d = mapper.toEmployeeWithTypedAccountsDto(e);
    assertThat(d.name, is("name"));
    assertThat(d.accounts.size(), is(2));
    assertThat(d.accounts.get(0), is(instanceOf(RedAccountDto.class)));
  }

  @Test
  public void testFromDtoViaSubClassMethod() {
    RedAccountDto d = new RedAccountDto(1L, true);
    lookup.store(1L, new RedAccount(1L, false));
    
    RedAccount o = mapper.fromDto(d);
    assertThat(o.getId(), is(1L));
    assertThat(o.isFoo(), is(true));
  }

  @Test
  public void testFromDtoViaBaseClassMethod() {
    RedAccountDto d = new RedAccountDto(1L, true);
    lookup.store(1L, new RedAccount(1L, false));
    
    RedAccount o = (RedAccount) mapper.fromDto((AccountDto) d);
    assertThat(o.getId(), is(1L));
    assertThat(o.isFoo(), is(true));
  }

  @Test
  public void testParentFromDto() {
    lookup.store(1L, new EmployeeWithTypedAccounts(1L, "name"));
    lookup.store(2L, new RedAccount(2L, true));
    lookup.store(3L, new BlueAccount(3L, false));
    
    EmployeeWithTypedAccountsDto d = new EmployeeWithTypedAccountsDto(1L, "name2", new ArrayList<AccountDto>());
    d.accounts.add(new RedAccountDto(2L, false));
    d.accounts.add(new BlueAccountDto(3L, true));
    
    EmployeeWithTypedAccounts o = mapper.fromDto(d);
    assertThat(o.getName(), is("name2"));
  }

}
