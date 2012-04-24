package com.bizo.dtonator.config;

import static joist.util.Copy.list;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.bizo.dtonator.properties.StubTypeOracle;

public class DtoConfigTest {

  private final StubTypeOracle oracle = new StubTypeOracle();
  private final Map<String, Object> root = new HashMap<String, Object>();
  private final RootConfig rootConfig = new RootConfig(oracle, root);
  private final Map<String, Object> config = new HashMap<String, Object>();

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
    addDto("FooDto", domain("Foo"), properties("-b, *"));
    // then we have only 1
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().size(), is(1));
  }

  @Test
  public void testPropertiesInclusion() {
    // given two properties
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    // and an override to skip b
    final Map<String, Object> map = new HashMap<String, Object>();
    map.put("domain", "Foo");
    map.put("properties", "a");
    // when asked
    final DtoConfig dc = new DtoConfig(oracle, rootConfig, "FooDto", map);
    // then we have only 1
    assertThat(dc.getProperties().size(), is(1));
  }

  @Test
  public void testPropertiesOverrideTypeAndIncludesAll() {
    // given a property of List and another string
    oracle.addProperty("com.domain.Foo", "a", "java.util.List");
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    // and an override for a and * to include b
    addDto("FooDto", domain("Foo"), properties("a ArrayList<Integer>, *"));
    // when asked
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().size(), is(2));
    assertThat(dc.getProperties().get(0).getName(), is("a"));
    assertThat(dc.getProperties().get(1).getName(), is("b"));
  }

  @Test
  public void testManualProperties() {
    // given no domain object, but manually specified properties
    addDto("FooDto", properties("id Integer, name String"));
    // then we have both properties
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().size(), is(2));
    assertThat(dc.getProperties().get(0).getName(), is("id"));
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.lang.Integer"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("java.lang.Integer"));
    assertThat(dc.getProperties().get(0).isExtension(), is(true));
    assertThat(dc.getProperties().get(1).getName(), is("name"));
    assertThat(dc.getProperties().get(1).getDtoType(), is("java.lang.String"));
    assertThat(dc.getProperties().get(1).getDomainType(), is("java.lang.String"));
    assertThat(dc.getProperties().get(1).isExtension(), is(true));
  }

  @Test
  public void testExtensionProperties() {
    // given a domain object with Foo.a
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    // but we specify both a and b
    addDto("FooDto", domain("Foo"), properties("a, b String"));
    // then we have both
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().size(), is(2));
    assertThat(dc.getProperties().get(0).getName(), is("a"));
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.lang.String"));
    assertThat(dc.getProperties().get(1).getName(), is("b"));
    assertThat(dc.getProperties().get(1).getDtoType(), is("java.lang.String"));
  }

  @Test
  public void testExtensionPropertiesFromJavaUtil() {
    // given an extension property of ArrayList
    addDto("FooDto", properties("a ArrayList<String>"));
    final DtoConfig dc = rootConfig.getDto("FooDto");
    // then we get the right type
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.util.ArrayList<String>"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("java.util.ArrayList<String>"));
    assertThat(dc.getProperties().get(0).isExtension(), is(true));
  }

  @Test
  public void testMappedOverridesFromJavaUtil() {
    // given a domain object with some children
    oracle.addProperty("com.domain.Foo", "children", "java.util.List<Child>");
    // and an override property of ArrayList
    addDto("FooDto", domain("Foo"), properties("children ArrayList<String>"));
    final DtoConfig dc = rootConfig.getDto("FooDto");
    // then we get the right type
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.util.ArrayList<String>"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("java.util.List<Child>"));
    assertThat(dc.getProperties().get(0).isExtension(), is(true));
  }

  @Test
  public void testMappedPropertiesThatAreValueTypes() {
    // given a domain object with a value types
    oracle.addProperty("com.domain.Foo", "a", "com.domain.ValueType");
    valueTypes().put("com.domain.ValueType", "com.dto.ValueType");
    addDto("FooDto", domain("Foo"));
    // then we know the right dto type
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().get(0).getDtoType(), is("com.dto.ValueType"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("com.domain.ValueType"));
  }

  @Test
  public void testMappedOverridesThatAreValueTypes() {
    // given a domain object with a value types
    oracle.addProperty("com.domain.Foo", "a", "com.domain.ValueType");
    valueTypes().put("com.domain.ValueType", "com.dto.ValueType");
    addDto("FooDto", domain("Foo"), properties("a"));
    // then we know the right dto type
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().get(0).getDtoType(), is("com.dto.ValueType"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("com.domain.ValueType"));
  }

  @Test
  public void testExtensionPropertiesThatAreValueTypes() {
    // given an extension property that is a ValueType
    addDto("FooDto", properties("value ValueType"));
    valueTypes().put("com.domain.values.ValueType", "com.dto.values.ValueType");
    final DtoConfig dc = rootConfig.getDto("FooDto");
    // map it back, with the fully qualified types
    assertThat(dc.getProperties().get(0).getDtoType(), is("com.dto.values.ValueType"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("com.domain.values.ValueType"));
    assertThat(dc.getProperties().get(0).isValueType(), is(true));
  }

  @Test
  public void testMappedPropertiesThatAreEnums() {
    oracle.setEnumValues("com.domain.Type", list("ONE", "TWO"));
    oracle.addProperty("com.domain.Foo", "type", "com.domain.Type");
    // when enums are mapped automatically
    addDto("FooDto", domain("Foo"));
    // they have the client/server types
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().get(0).getDtoType(), is("com.dto.Type"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("com.domain.Type"));
  }

  @Test
  public void testExtensionPropertiesThatAreEnums() {
    oracle.setEnumValues("com.domain.Type", list("ONE", "TWO"));
    // when an extension property references an enum
    addDto("FooDto", properties("type Type"));
    // it's fully qualified
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().get(0).getDtoType(), is("com.dto.Type"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("com.domain.Type"));
  }

  @Test
  public void testExtensionPropertiesThatAreDtos() {
    // when an extension property references another dto
    addDto("FooDto", properties("bar BarDto"));
    addDto("BarDto");
    // it's fully qualified
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().get(0).getDtoType(), is("com.dto.BarDto"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("com.dto.BarDto"));
  }

  @Test
  public void testMappedPropertiesThatAreEntities() {
    // when Foo references a parent Bar
    oracle.addProperty("com.domain.Foo", "bar", "com.domain.Bar");
    // and it's not explicitly included in properties
    addDto("FooDto", domain("Foo"));
    addDto("BarDto");
    // then we skip it
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().size(), is(0));
  }

  @Test
  public void testMappedOverridesThatAreEntities() {
    // when referencing BarDto
    oracle.addProperty("com.domain.Foo", "bar", "com.domain.Bar");
    addDto("FooDto", domain("Foo"), properties("bar")); // leave off BarDto
    addDto("BarDto");
    // it's fully qualified
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().get(0).getDtoType(), is("com.dto.BarDto"));
    assertThat(dc.getProperties().get(0).isExtension(), is(false));
  }

  @Test
  public void testMappedOverridesThatAreDtos() {
    // when referencing BarDto
    oracle.addProperty("com.domain.Foo", "bar", "com.domain.Bar");
    addDto("FooDto", domain("Foo"), properties("bar BarDto")); // now include BarDto
    addDto("BarDto");
    // it's fully qualified
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().get(0).getDtoType(), is("com.dto.BarDto"));
    assertThat(dc.getProperties().get(0).isExtension(), is(false));
  }

  @Test
  public void testMappedPropertiesThatAreReadOnly() {
    // given Foo.a
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    // and mapped as read only
    addDto("FooDto", domain("Foo"), properties("~a"));
    // then we know it's read only
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().size(), is(1));
    assertThat(dc.getProperties().get(0).isReadOnly(), is(true));
  }

  @Test
  public void testExtensionPropertiesThatAreReadOnly() {
    // given an extension property that is read only
    addDto("FooDto", properties("~a String"));
    final DtoConfig dc = rootConfig.getDto("FooDto");
    // then we know it's read only
    assertThat(dc.getProperties().get(0).isReadOnly(), is(true));
  }

  @Test
  public void testMappedPropertiesThatAreListsOfEntities() {
    // given a parent and child
    oracle.addProperty("com.domain.Parent", "name", "java.lang.String");
    oracle.addProperty("com.domain.Parent", "children", "java.util.List<com.domain.Child>");
    oracle.addProperty("com.domain.Child", "id", "java.lang.Integer");
    // and the child dto has an entry in the yaml file
    addDto("ChildDto");
    // and but the parent doesn't opt in the children
    addDto("ParentDto", domain("Parent"));
    // then it only has the name property
    final DtoConfig dc = rootConfig.getDto("ParentDto");
    assertThat(dc.getProperties().size(), is(1));
    assertThat(dc.getProperties().get(0).getName(), is("name"));
  }

  @Test
  public void testMappedOverridesThatAreListsOfEntities() {
    // given a parent and child
    oracle.addProperty("com.domain.Parent", "children", "java.util.List<com.domain.Child>");
    oracle.addProperty("com.domain.Child", "id", "java.lang.Integer");
    // and the child dto has an entry in the yaml file
    addDto("ChildDto");
    // and explicitly asking for children
    addDto("ParentDto", domain("Parent"), properties("children")); // leave off List<ChildDto>
    // then we map Child to the ChildDto
    final DtoConfig dc = rootConfig.getDto("ParentDto");
    assertThat(dc.getProperties().size(), is(1));
    assertThat(dc.getProperties().get(0).getName(), is("children"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("java.util.List<com.domain.Child>"));
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.util.ArrayList<com.dto.ChildDto>"));
  }

  @Test
  public void testMappedOverridesThatAreListsOfDtos() {
    // given a parent and child
    oracle.addProperty("com.domain.Parent", "children", "java.util.List<com.domain.Child>");
    oracle.addProperty("com.domain.Child", "id", "java.lang.Integer");
    // and the child dto has an entry in the yaml file
    addDto("ChildDto");
    // and explicitly asking for children
    addDto("ParentDto", domain("Parent"), properties("children List<ChildDto>")); // include List<ChildDto>
    // then we map Child to the ChildDto
    final DtoConfig dc = rootConfig.getDto("ParentDto");
    assertThat(dc.getProperties().size(), is(1));
    assertThat(dc.getProperties().get(0).getName(), is("children"));
    assertThat(dc.getProperties().get(0).getDomainType(), is("java.util.List<com.domain.Child>"));
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.util.ArrayList<com.dto.ChildDto>"));
  }

  @Test
  public void testExtensionPropertiesThatAreListsOfDtos() {
    // when referencing BarDtos
    addDto("FooDto", domain("Foo"), properties("bars ArrayList<BarDto>"));
    addDto("BarDto");
    // it's fully qualified
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().get(0).getDtoType(), is("java.util.ArrayList<com.dto.BarDto>"));
  }

  @Test
  public void testMappedAliasedProperties() {
    // given a domain object with longPropertyName
    oracle.addProperty("com.domain.Foo", "longPropertyName", "java.lang.String");
    // and aliased to name
    addDto("FooDto", domain("Foo"), properties("name(longPropertyName)"));
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().get(0).getName(), is("name"));
    assertThat(dc.getProperties().get(0).getGetterMethodName(), is("getLongPropertyName"));
  }

  @Test
  public void testSortedAlphabetically() {
    // given three properties
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "id", "java.lang.Integer");
    addDto("FooDto", domain("Foo"));
    // then we've sorted them
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().size(), is(3));
    assertThat(dc.getProperties().get(0).getName(), is("id"));
    assertThat(dc.getProperties().get(1).getName(), is("a"));
    assertThat(dc.getProperties().get(2).getName(), is("b"));
  }

  @Test
  public void testSortedByConfigFirst() {
    // given four properties, initially out of order
    oracle.addProperty("com.domain.Foo", "d", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "c", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "b", "java.lang.String");
    oracle.addProperty("com.domain.Foo", "a", "java.lang.String");
    // and overrides putting d, c first
    addDto("FooDto", domain("Foo"), properties("d, c String, b, a"));
    // then we've sorted them
    final DtoConfig dc = rootConfig.getDto("FooDto");
    assertThat(dc.getProperties().size(), is(4));
    assertThat(dc.getProperties().get(0).getName(), is("d"));
    assertThat(dc.getProperties().get(1).getName(), is("c"));
    assertThat(dc.getProperties().get(2).getName(), is("b"));
    assertThat(dc.getProperties().get(3).getName(), is("a"));
  }

  private void addDto(final String simpleName, final Entry... entries) {
    final Map<String, Object> map = new HashMap<String, Object>();
    for (final Entry entry : entries) {
      map.put(entry.name, entry.value);
    }
    root.put(simpleName, map);
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> valueTypes() {
    Object value = config.get("valueTypes");
    if (value == null) {
      value = new HashMap<String, String>();
      config.put("valueTypes", value);
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
