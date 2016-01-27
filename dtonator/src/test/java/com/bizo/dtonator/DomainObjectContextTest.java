package com.bizo.dtonator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class DomainObjectContextTest {

  @Test
  public void shouldUseReferenceEquality() {
    DomainObjectContext c = new DomainObjectContext();
    TestDto d1 = new TestDto("d");
    TestDto d2 = new TestDto("d");
    c.store(d1, new Object());
    assertThat(c.get(d2), is(nullValue()));
  }

  public static class TestDto {
    private final String name;

    public TestDto(String name) {
      this.name = name;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof TestDto && ((TestDto) other).name.equals(name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }

}
