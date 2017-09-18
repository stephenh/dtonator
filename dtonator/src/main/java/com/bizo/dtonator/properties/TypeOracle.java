package com.bizo.dtonator.properties;

import java.util.List;

/**
 * Abstracts finding metadata about classes.
 * 
 * The main implementation is {@link ReflectionTypeOracle} but tests can use stubs instead.
 */
public interface TypeOracle {

  List<Prop> getProperties(final String className, boolean excludeInherited);

  boolean isEnum(final String className);

  boolean isAbstract(final String className);

  List<String> getEnumValues(final String className);

}
