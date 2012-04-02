package com.bizo.dtonator.properties;

import java.util.List;

public interface TypeOracle {

  List<Prop> getProperties(final String className);

  boolean isEnum(final String className);

  List<String> getEnumValues(final String className);

}
