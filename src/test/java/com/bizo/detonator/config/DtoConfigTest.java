package com.bizo.detonator.config;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.junit.Test;

import com.bizo.detonator.properties.TypeOracle;

public class DtoConfigTest {

  private final TypeOracle oracle = null;
  private final Map<String, Object> root = newHashMap();
  private final RootConfig rootConfig = new RootConfig(oracle, root);

  @Test
  public void testAllProperties() {
    final Map<String, Object> map = newHashMap();
    new DtoConfig(oracle, rootConfig, "FooDto", map);
  }

}
