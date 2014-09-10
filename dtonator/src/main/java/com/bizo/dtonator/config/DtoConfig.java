package com.bizo.dtonator.config;

import static com.bizo.dtonator.Names.listType;
import static com.bizo.dtonator.Names.simple;
import static java.lang.Boolean.TRUE;
import static joist.util.Copy.list;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.substringBefore;
import static org.apache.commons.lang.StringUtils.substringBetween;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bizo.dtonator.properties.Prop;
import com.bizo.dtonator.properties.TypeOracle;

public class DtoConfig {

  private final TypeOracle oracle;
  private final RootConfig root;
  private final String simpleName;
  private final Map<String, Object> map;
  private List<DtoProperty> properties;

  public DtoConfig(final TypeOracle oracle, final RootConfig root, final String simpleName, final Object map) {
    this.oracle = oracle;
    this.root = root;
    this.simpleName = simpleName;
    this.map = (map == null) ? new HashMap<String, Object>() : YamlUtils.<String, Object> ensureMap(map);
  }

  public List<DtoProperty> getProperties() {
    if (properties == null) {
      properties = list();
      final List<PropConfig> pcs = getPropertiesConfig();
      if (getDomainType() != null) {
        addPropertiesFromDomainObject(pcs);
        addChainedPropertiesFromDomainObject(pcs);
      }
      addLeftOverExtensionProperties(pcs);
      sortProperties(pcs, properties);
    }
    return properties;
  }

  public List<DtoProperty> getInheritedProperties() {
    List<DtoProperty> p = list();
    for (DtoConfig base : getBaseDtos()) {
      p.addAll(base.getProperties());
    }
    return p;
  }

  /** @return the base dtos, with the root dto first. */
  private List<DtoConfig> getBaseDtos() {
    List<DtoConfig> p = list();
    String currentName = getBaseDtoSimpleName();
    while (currentName != null) {
      DtoConfig dto = root.getDto(currentName);
      p.add(dto);
      currentName = dto.getBaseDtoSimpleName();
    }
    Collections.reverse(p);
    return p;
  }

  public List<DtoProperty> getAllProperties() {
    return list(getInheritedProperties()).with(getProperties());
  }

  public String getSimpleName() {
    return simpleName;
  }

  public String getDtoType() {
    return root.getDtoPackage() + "." + getSimpleName();
  }

  /** @return whether the user wants a public no-arg constructor, defaults to {@code false}. */
  public boolean shouldAddPublicConstructor() {
    return TRUE.equals(map.get("publicConstructor"));
  }

  /** @return the specific annotations this DTO should implement. */
  public List<String> getAnnotations() {
    final List<String> annotations = list();
    if (map.containsKey("annotations")) {
      for (final String annotation : ((String) map.get("annotations")).split(", ?")) {
        annotations.add("@" + annotation);
      }
    }
    return annotations;
  }

  /** @return the specific + common interfaces this DTO should implement. */
  public List<String> getInterfaces() {
    final List<String> interfaces = list();
    if (map.containsKey("interfaces")) {
      interfaces.addAll(list(((String) map.get("interfaces")).split(", ?")));
    }
    interfaces.addAll(root.getCommonInterfaces());
    return interfaces;
  }

  /** @return this DTOs base class simple name, or {@code null} */
  public String getBaseDtoSimpleName() {
    return (String) map.get("extends");
  }

  /** @return this DTOs base class, or {@code null} */
  public DtoConfig getBaseDto() {
    if (getBaseDtoSimpleName() != null) {
      return root.getDto(getBaseDtoSimpleName());
    }
    return null;
  }

  /** @return subclasses, with the leaf classes (e.g. grand children) first */
  public List<DtoConfig> getSubClassDtos() {
    List<DtoConfig> subClasses = list();
    List<DtoConfig> toProbe = list(this);
    while (!toProbe.isEmpty()) {
      DtoConfig current = toProbe.remove(0);
      // scan for subclasses of current
      for (DtoConfig other : root.getDtos()) {
        if (current.getSimpleName().equals(other.getBaseDtoSimpleName())) {
          subClasses.add(other);
          toProbe.add(other);
        }
      }
    }
    Collections.reverse(subClasses);
    return subClasses;
  }

  public boolean includeBeanMethods() {
    return TRUE.equals(map.get("beanMethods")) || root.includeBeanMethods();
  }

  /** @return the domain type backing this DTO, or {@code null} if it's standalone. */
  public String getDomainType() {
    final String rawValue = (String) map.get("domain");
    if (rawValue == null) {
      return null;
    }
    if (rawValue.contains(".")) {
      return rawValue;
    } else {
      return root.getDomainPackage() + "." + rawValue;
    }
  }

  /** @return properties to include for equality or {@code null} */
  public List<String> getEquality() {
    final Object value = map.get("equality");
    if (value == null) {
      return null;
    }
    if (!(value instanceof String)) {
      throw new IllegalArgumentException("Expected a string for equality key");
    } else if ("*".equals(value)) {
      final List<String> names = list();
      for (final DtoProperty p : getProperties()) {
        names.add(p.getName());
      }
      return names;
    } else {
      return Arrays.asList(((String) value).split(",? "));
    }
  }

  /** @return whether the dto wants a tessell model */
  public boolean includeTessellModel() {
    return TRUE.equals(map.get("tessellModel"));
  }

  public boolean isEnum() {
    return !isManualDto() && oracle.isEnum(getDomainType());
  }

  public boolean isAbstract() {
    return !isManualDto() && oracle.isAbstract(getDomainType());
  }

  public List<String> getEnumValues() {
    return oracle.getEnumValues(getDomainType());
  }

  public boolean isManualDto() {
    return getDomainType() == null;
  }

  /** @return a list of properties that we want to force mapper methods for */
  public List<String> getForcedMappers() {
    return YamlUtils.parseExpectedStringToList("forceMapperMethods", map.get("forceMapperMethods"));
  }

  public boolean requiresMapperType() {
    return !isManualDto() && (hasExtensionProperties() || !getForcedMappers().isEmpty());
  }

  public boolean hasIdProperty() {
    for (final DtoProperty p : getAllProperties()) {
      if (p.getName().equals("id")) {
        return true;
      }
    }
    return false;
  }

  public boolean hasExtensionProperties() {
    for (final DtoProperty p : getProperties()) {
      if (p.isExtension()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return getSimpleName();
  }

  private void addChainedPropertiesFromDomainObject(final List<PropConfig> pcs) {
    for (final Prop p : oracle.getProperties(getDomainType())) {
      // if we found "getFoo()/setFoo()" via reflection, look for a "fooId" prop config,
      // that would tell us the user wants to get/set Foo as its id
      final PropConfig pc = findChainedPropConfig(pcs, p.name);
      if (pc == null) {
        continue;
      }
      final boolean hasGetterSetter = p.getterMethodName != null && p.setterNameMethod != null;
      final boolean isDomainObject = isDomainObject(oracle, p.type);
      final boolean dtoTypesMatch = pc.type == null || pc.type.equals("java.lang.Long"); // we currently only support ids (longs)
      final boolean alreadyMapped = pc.mapped; // found a fooId prop config, but it mapped to an existing getFooId/setFooId domain property
      if (hasGetterSetter && isDomainObject && dtoTypesMatch && !alreadyMapped) {
        pc.markNotExtensionProperty();
        properties.add(new DtoProperty(//
          oracle,
          root,
          this,
          pc != null ? pc.name : p.name, // use the potentially aliased if we have one
          pc != null ? pc.isReadOnly : p.readOnly,
          true,
          "java.lang.Long",
          p.type,
          p.getterMethodName,
          p.setterNameMethod));
      }
    }
  }

  private void addPropertiesFromDomainObject(final List<PropConfig> pcs) {
    final boolean includeUnmapped = pcs.size() == 0 || hasPropertiesInclude();
    for (final Prop p : oracle.getProperties(getDomainType())) {
      final PropConfig pc = findPropConfig(pcs, p.name);
      if (pc != null) {
        // if we found a property in the oracle, we know this isn't an extension
        pc.markNotExtensionProperty();
      }

      if (doNotMap(p, pc, includeUnmapped)) {
        continue;
      }

      // domainType has to be p.type, it came from reflection (right?)
      final String domainType = p.type;

      boolean extension = false;

      final String dtoType;
      if (pc != null && pc.type != null) {
        // the user provided a PropConfig with an explicit type
        if (root.getDto(pc.type) != null) {
          // the type was FooDto, we need to fully qualify it
          dtoType = root.getDto(pc.type).getDtoType();
        } else if (isListOfDtos(root, pc.type)) {
          // the type was java.util.ArrayList<FooDto>, resolve the dto package
          dtoType = "java.util.ArrayList<" + root.getDtoPackage() + "." + listType(pc.type) + ">";
        } else {
          dtoType = pc.type;
          extension = !dtoType.equals(domainType);
        }
        // TODO pc.type might ValueType (client-side), need to fully quality it?
      } else {
        // no PropConfig with an explicit type, so we infer the dto type from the domain type
        if (root.getValueTypeForDomainType(domainType) != null) {
          dtoType = root.getValueTypeForDomainType(domainType).dtoType;
        } else if (oracle.isEnum(domainType)) {
          dtoType = root.getDtoPackage() + "." + simple(domainType);
        } else if (isListOfEntities(root, domainType)) {
          // only map lists of entities if it was included in properties
          if (pc != null) {
            dtoType = "java.util.ArrayList<" + guessDtoTypeForDomainType(root, listType(domainType)).getDtoType() + ">";
          } else {
            dtoType = null;
          }
        } else if (domainType.startsWith(root.getDomainPackage())) {
          // only map entities if it was included in properties
          if (pc != null) {
            dtoType = guessDtoTypeForDomainType(root, domainType).getDtoType();
          } else {
            dtoType = null;
          }
        } else {
          dtoType = domainType;
        }
      }

      if (dtoType == null) {
        continue;
      }

      String name = pc != null ? pc.name : p.name; // use the potentially aliased if we have one
      properties.add(new DtoProperty(//
        oracle,
        root,
        this,
        name,
        pc != null ? pc.isReadOnly : p.readOnly,
        false,
        dtoType,
        domainType,
        extension ? null : p.getterMethodName,
        extension ? null : p.setterNameMethod));
    }
  }

  // now look for extension properties (did not exist in the domain object)
  private void addLeftOverExtensionProperties(final List<PropConfig> pcs) {
    for (final PropConfig pc : pcs) {
      if (pc.mapped) {
        continue;
      }
      if (pc.type == null) {
        throw new IllegalStateException("type is required for extension properties: " + pc.name);
      }

      final String dtoType;

      // Nothing was found via reflection against the domain object (if it was available)
      // This means the user must provide a mapper method, now it's just a mapper of what
      // the signature of the mapper method will be.
      final String domainType;

      if (root.getDto(pc.type) != null) {
        dtoType = root.getDto(pc.type).getDtoType();
        // this is an extension method, so the user must do the mapping...
        domainType = dtoType;
      } else if (isListOfDtos(root, pc.type)) {
        // the type was java.util.ArrayList<FooDto>, resolve the dto package
        dtoType = "java.util.ArrayList<" + root.getDtoPackage() + "." + listType(pc.type) + ">";
        // loosen the type to List, otherwise the user still has to provide the dtos themselves
        domainType = "java.util.List<" + root.getDtoPackage() + "." + listType(pc.type) + ">";
      } else if (root.getValueTypeForDtoType(pc.type) != null) {
        dtoType = root.getValueTypeForDtoType(pc.type).dtoType; // fully qualified
        domainType = root.getValueTypeForDtoType(pc.type).domainType;
      } else if (oracle.isEnum(root.getDomainPackage() + "." + pc.type)) {
        dtoType = root.getDtoPackage() + "." + pc.type;
        domainType = root.getDomainPackage() + "." + pc.type;
      } else {
        // assume pc.type is the dto type
        dtoType = pc.type;
        domainType = dtoType;
      }
      properties.add(new DtoProperty(//
        oracle,
        root,
        this,
        pc.name,
        pc.isReadOnly,
        false,
        dtoType,
        domainType,
        null,
        null));
    }
  }

  /** @return the `properties: a, b` as parsed {@link PropConfig}, skipping the `*` character. */
  private List<PropConfig> getPropertiesConfig() {
    final List<PropConfig> args = list();
    for (final String eachValue : getPropertiesConfigRaw()) {
      if (!"*".equals(eachValue)) {
        args.add(new PropConfig(eachValue));
      }
    }
    return args;
  }

  /** @return did the user include {@code properties: *}. */
  private boolean hasPropertiesInclude() {
    return getPropertiesConfigRaw().contains("*");
  }

  /** @return the raw strings of parsing {@code properties} by {@code ,} or an empty list */
  private List<String> getPropertiesConfigRaw() {
    return YamlUtils.parseExpectedStringToList("properties", map.get("properties"));
  }

  /** Small abstraction around the property strings in the YAML file. */
  static class PropConfig {
    final String name;
    final String domainName;
    final String type;
    final boolean isExclusion;
    final boolean isReadOnly;
    boolean mapped = false;

    PropConfig(final String value) {
      // examples:
      // foo
      // foo Bar
      // ~foo
      // foo(zaz) Bar
      String _name;
      String _domainName = null;
      if (value.contains(" ")) {
        final String[] parts = splitIntoNameAndType(value);
        _name = parts[0];
        type = TypeName.javaLangOrUtilPrefixIfNecessary(parts[1]);
        isExclusion = false;
      } else if (value.startsWith("-")) {
        _name = value.substring(1);
        type = null;
        isExclusion = true;
      } else {
        _name = value;
        type = null;
        isExclusion = false;
      }
      if (_name.startsWith("~")) {
        isReadOnly = true;
        _name = _name.substring(1);
      } else {
        isReadOnly = false;
      }
      if (_name.endsWith(")")) {
        _domainName = substringBetween(_name, "(", ")");
        _name = substringBefore(_name, "(");
      }
      name = _name;
      domainName = defaultString(_domainName, _name);
    }

    private void markNotExtensionProperty() {
      mapped = true;
    }

    @Override
    public String toString() {
      return (isExclusion ? "-" : "") + name + (type == null ? "" : " " + type);
    }

    private static String[] splitIntoNameAndType(final String value) {
      final String[] parts = value.split(" ");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Value '<name> <type>': " + value);
      }
      return new String[] { parts[0], parts[1] };
    }
  }

  private static boolean doNotMap(final Prop p, final PropConfig pc, final boolean includeUnmapped) {
    return (pc != null && pc.isExclusion) // user configured explicit exclusion
      || (pc == null && !includeUnmapped); // user left it out and we're not including everything
  }

  /** Sorts the properties based on their order in the YAML file, whether they're {@code id}, or alphabetically. */
  private static void sortProperties(final List<PropConfig> pcs, final List<DtoProperty> properties) {
    Collections.sort(properties, new Comparator<DtoProperty>() {
      public int compare(final DtoProperty o1, final DtoProperty o2) {
        final PropConfig pc1 = findPropConfig(pcs, o1.getName());
        final PropConfig pc2 = findPropConfig(pcs, o2.getName());
        if (pc1 != null && pc2 != null) {
          return indexOfPropConfig(pcs, o1.getName()) - indexOfPropConfig(pcs, o2.getName());
        } else if (pc1 != null && pc2 == null) {
          return -1;
        } else if (pc1 == null && pc2 != null) {
          return 1;
        } else {
          if ("id".equals(o1.getName()) && "id".equals(o2.getName())) {
            return 0;
          } else if ("id".equals(o1.getName())) {
            return -1;
          } else if ("id".equals(o2.getName())) {
            return 1;
          }
          return o1.getName().compareTo(o2.getName());
        }
      }
    });
  }

  private static PropConfig findPropConfig(final List<PropConfig> pcs, final String name) {
    for (final PropConfig pc : pcs) {
      if (pc.domainName.equals(name)) {
        return pc;
      }
    }
    return null;
  }

  private static PropConfig findChainedPropConfig(final List<PropConfig> pcs, final String name) {
    for (final PropConfig pc : pcs) {
      if (pc.domainName.equals(name + "Id")) { // hardcoded to id for now
        return pc;
      }
    }
    return null;
  }

  private static int indexOfPropConfig(final List<PropConfig> pcs, final String name) {
    int i = 0;
    for (final PropConfig pc : pcs) {
      if (pc.name.equals(name)) {
        return i;
      }
      i++;
    }
    return -1;
  }

  private static DtoConfig guessDtoTypeForDomainType(final RootConfig root, final String domainType) {
    final String guessedSimpleName = simple(domainType) + "Dto";
    final DtoConfig childConfig = root.getDto(guessedSimpleName);
    if (childConfig == null) {
      throw new IllegalStateException("Could not find a default dto " + guessedSimpleName + " for " + domainType);
    }
    return childConfig;
  }

  static boolean isListOfEntities(final RootConfig config, final String domainType) {
    return domainType.startsWith("java.util.List<" + config.getDomainPackage());
  }

  static boolean isEntity(final RootConfig config, final String domainType) {
    return domainType.startsWith(config.getDomainPackage());
  }

  static boolean isListOfDtos(final RootConfig config, final String pcType) {
    // assume the user wanted List to be ArrayList
    if ((pcType.startsWith("java.util.ArrayList<") || pcType.startsWith("java.util.List<")) && pcType.endsWith(">")) {
      return config.getDto(simple(listType(pcType))) != null;
    }
    return false;
  }

  static boolean isDomainObject(final TypeOracle oracle, final String type) {
    for (final Prop p : oracle.getProperties(type)) {
      if (p.name.equals("id") && p.type.equals("java.lang.Long")) {
        return true;
      }
    }
    return false;
  }
}
