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

}
