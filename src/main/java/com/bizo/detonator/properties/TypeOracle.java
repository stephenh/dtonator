package com.bizo.detonator.properties;

import java.util.List;

import com.bizo.detonator.config.DtoProperty;

public interface TypeOracle {

  List<DtoProperty> getProperties(final String className);

  boolean isEnum(final String className);

  List<String> getEnumValues(final String className);

}
