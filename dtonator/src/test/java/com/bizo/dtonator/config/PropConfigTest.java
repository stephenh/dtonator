package com.bizo.dtonator.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.bizo.dtonator.config.DtoConfig.PropConfig;

public class PropConfigTest {

  @Test
  public void testName() {
    final PropConfig pc = new PropConfig("foo");
    assertThat(pc.name, is("foo"));
    assertThat(pc.type, is(nullValue()));
  }

  @Test
  public void testNameWithType() {
    final PropConfig pc = new PropConfig("foo Bar");
    assertThat(pc.name, is("foo"));
    assertThat(pc.type, is("Bar"));
  }

  @Test
  public void testNameWithJavaLangType() {
    final PropConfig pc = new PropConfig("foo Integer");
    assertThat(pc.name, is("foo"));
    assertThat(pc.type, is("java.lang.Integer"));
  }

  @Test
  public void testNameWithJavaUtilType() {
    final PropConfig pc = new PropConfig("foo ArrayList<Integer>");
    assertThat(pc.name, is("foo"));
    assertThat(pc.type, is("java.util.ArrayList<java.lang.Integer>"));
  }

}
