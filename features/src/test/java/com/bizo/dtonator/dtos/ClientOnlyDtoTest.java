package com.bizo.dtonator.dtos;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ClientOnlyDtoTest {

  @Test
  public void testCstr() {
    final ClientOnlyDto d = new ClientOnlyDto(1l, "asdf");
    assertThat(d.id, is(1l));
    assertThat(d.name, is("asdf"));
  }

  @Test
  public void testEquals() {
    final ClientOnlyDto d1 = new ClientOnlyDto(1l, "asdf1");
    final ClientOnlyDto d2 = new ClientOnlyDto(1l, "asdf2");
    assertThat(d1.equals(d2), is(true));
    assertThat(d1.hashCode() == d2.hashCode(), is(true));
  }

}
