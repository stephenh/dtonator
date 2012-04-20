package com.bizo.dtonator.config;

import static com.google.common.collect.Maps.newHashMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.bizo.dtonator.properties.StubTypeOracle;

public class DtoConfigTest {

  private final StubTypeOracle oracle = new StubTypeOracle();
  private final Map<String, Object> root = newHashMap();
  private final RootConfig rootConfig = new RootConfig(oracle, root);
  private final Map<String, Object> config = newHashMap();

  @Before
  public void setupRootConfig() {
    config.put("domainPackage", "com.domain");
    config.put("dtoPackage", "com.dto");
    root.put("config", config);
  }

  @Test
  public void testAllProperties() {
    // given two properties
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    // and no overrides
    addDto("FooDto", domain("Foo"));
    // then we have both
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().size(), is(2));
  }

  @Test
  public void testPropertiesExclusion() {
    // given two properties
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    // and an override to skip b
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    map.put("properties", "-b, *");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then we have only 1
    assertThat(dc.getProperties().size(), is(1));
  }

  @Test
  public void testPropertiesInclusion() {
    // given two properties
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    // and an override to skip b
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    map.put("properties", "a");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then we have only 1
    assertThat(dc.getProperties().size(), is(1));
  }

  @Test
  public void testPropertiesOverrideType() {
    // given a property of just List
    oracle.addProperty("com.domain.Foo", "a", "java.util.List");
    // and an override to change it to ArrayList<Integer>
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    map.put("properties", "a ArrayList<Integer>");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    assertThat(dc.getProperties().size(), is(1));
    assertThat(dc.getProperties().get(0).getName(), is("a"));
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.util.ArrayList<Integer>"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("java.util.ArrayList<Integer>"));
  }

  @Test
  public void testPropertiesOverrideTypeAndIncludesAll() {
    // given a property of List and another string
    oracle.addProperty("com.domain.Foo", "a", "java.util.List");
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    // and an override for a and * to include b
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    map.put("properties", "a ArrayList<Integer>, *");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    assertThat(dc.getProperties().size(), is(2));
    assertThat(dc.getProperties().get(0).getName(), is("a"));
    assertThat(dc.getProperties().get(1).getName(), is("b"));
  }

  @Test
  public void testManualProperties() {
    // given no domain object set
    final Map<String, Object> map = newHashMap();
    // map.put("domain", "Foo");
    // but manually speified properties
    map.put("properties", "id Integer, name String");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then we have both
    assertThat(dc.getProperties().size(), is(2));
    assertThat(dc.getProperties().get(0).getName(), is("id"));
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.lang.Integer"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("java.lang.Integer"));
    assertThat(dc.getProperties().get(1).getName(), is("name"));
    assertThat(dc.getProperties().get(1).getDtoType(), is("java.lang.String"));
  }

  @Test
  public void testExtensionProperties() {
    // given a domain object with Foo.a
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    // but we specify both a and b
    map.put("properties", "a, b String");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then we have both
    assertThat(dc.getProperties().size(), is(2));
    assertThat(dc.getProperties().get(0).getName(), is("a"));
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.lang.String"));
    assertThat(dc.getProperties().get(1).getName(), is("b"));
    assertThat(dc.getProperties().get(1).getDtoType(), is("java.lang.String"));
  }

  @Test
  public void testExtensionPropertiesFromJavaUtil() {
    // given a domain object Foo
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    // and an extension property of ArrayList
    map.put("properties", "a ArrayList<String>");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then we get the right type
    assertThat(dc.getProperties().size(), is(1));
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.util.ArrayList<String>"));
  }

  @Test
  public void testSortedAlphabetically() {
    // given two properties
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    // and no overrides
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then we've sorted them
    assertThat(dc.getProperties().size(), is(2));
    assertThat(dc.getProperties().get(0).getName(), is("a"));
    assertThat(dc.getProperties().get(1).getName(), is("b"));
  }

  @Test
  public void testSortedByConfigFirst() {
    // given four properties, initially out of order
    oracle.addProperty("com.domain.Foo", "d", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "c", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    // and overrides putting d, c first
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    map.put("properties", "d, c String, b, a");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then we've sorted them
    assertThat(dc.getProperties().size(), is(4));
    assertThat(dc.getProperties().get(0).getName(), is("d"));
    assertThat(dc.getProperties().get(1).getName(), is("c"));
    assertThat(dc.getProperties().get(2).getName(), is("b"));
    assertThat(dc.getProperties().get(3).getName(), is("a"));
  }

  @Test
  public void testReadOnly() {
    // given Foo.a
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    // and marked as read only
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    map.put("properties", "~a");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then know it's read only
    assertThat(dc.getProperties().size(), is(1));
    assertThat(dc.getProperties().get(0).isReadOnly(), is(true));
  }

  @Test
  public void testUserTypes() {
    // given a domain object with a user type
    oracle.addProperty("com.domain.Foo", "a", "com.domain.UserType");
    // and the user type configured
    getUserTypes(config).put("com.domain.UserType", "com.dto.UserType");
    // and no overrides
    final Map<String, Object> map = newHashMap();
    map.put("domain", "Foo");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then we know the right dto type
    assertThat(dc.getProperties().get(0).getDtoType(), is("com.dto.UserType"));
  }

  @Test
  public void testChildDomainObjectAreSkippedUnlessSpecified() {
    // given a parent and child
    oracle.addProperty("com.domain.Parent", "name", "java.lang.String");
    oracle.addProperty("com.domain.Parent", "children", "java.util.List<com.domain.Child>");
    oracle.addProperty("com.domain.Child", "id", "java.lang.Integer");
    // and the child dto has an entry in the yaml file
    addDto("ChildDto");
    // and but the parent doesn't out in the children
    addDto("ParentDto", domain("Parent"));
    // then it only has the name property
    final DtoConfig dc = rootConfig.getDto("ParentDto");
    assertThat(dc.getProperties().size(), is(1));
    assertThat(dc.getProperties().get(0).getName(), is("name"));
  }

  @Test
  public void testChildDomainObject() {
    // given a parent and child
    oracle.addProperty("com.domain.Parent", "children", "java.util.List<com.domain.Child>");
    oracle.addProperty("com.domain.Child", "id", "java.lang.Integer");
    // and the child dto has an entry in the yaml file
    addDto("ChildDto");
    // and explicitly asking for children
    addDto("ParentDto", domain("Parent"), properties("children"));
    // then we map Child to the ChildDto
    final DtoConfig dc = rootConfig.getDto("ParentDto");
    assertThat(dc.getProperties().size(), is(1));
    assertThat(dc.getProperties().get(0).getName(), is("children"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("java.util.List<com.domain.Child>"));
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.util.ArrayList<com.dto.ChildDto>"));
  }

  private void addDto(final String simpleName, final Entry... entries) {
    final Map<String, Object> map = newHashMap();
    for (final Entry entry : entries) {
      map.put(entry.name, entry.value);
    }
    root.put(simpleName, map);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> getUserTypes(final Map<String, Object> config) {
    Object value = config.get("userTypes");
    if (value == null) {
      value = newHashMap();
      config.put("userTypes", value);
    }
    return (Map<String, String>) value;
  }

  private static Entry domain(final String value) {
    return new Entry("domain", value);
  }

  private static Entry properties(final String value) {
    return new Entry("properties", value);
  }

  private static class Entry {
    private final String name;
    private final Object value;

    private Entry(final String name, final Object value) {
      this.name = name;
      this.value = value;
    }
  }

}
