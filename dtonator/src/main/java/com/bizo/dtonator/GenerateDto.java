package com.bizo.dtonator;

import static com.bizo.dtonator.Names.mapperFieldName;
import static com.bizo.dtonator.Names.mapperInterface;
import static com.bizo.dtonator.Names.simple;
import static joist.sourcegen.Argument.arg;
import static joist.util.Copy.list;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.uncapitalize;

import java.util.List;

import joist.sourcegen.Argument;
import joist.sourcegen.GClass;
import joist.sourcegen.GDirectory;
import joist.sourcegen.GMethod;
import joist.util.Join;

import com.bizo.dtonator.config.DtoConfig;
import com.bizo.dtonator.config.DtoProperty;
import com.bizo.dtonator.config.RootConfig;

public class GenerateDto {

  private final GDirectory out;
  private final GClass mapper;
  private final RootConfig config;
  private final List<String> takenToDtoOverloads;
  private final DtoConfig dto;
  private final GClass gc;

  public GenerateDto(final RootConfig config, final GDirectory out, final GClass mapper, final List<String> takenToDtoOverloads, final DtoConfig dto) {
    this.config = config;
    this.out = out;
    this.mapper = mapper;
    this.takenToDtoOverloads = takenToDtoOverloads;
    this.dto = dto;
    gc = out.getClass(dto.getDtoType());
  }

  public void generate() {
    System.out.println("Generating " + dto.getSimpleName());
    addBaseClassIfNeeded();
    addAnnotations();
    addInterfaces();
    addDtoFields();
    addDefaultConstructor();
    addFullConstructor();
    addEqualityIfNeeded();
    addToString();
    addToFromMethodsToMapperIfNeeded();
    createMapperTypeIfNeeded();
  }

  private void addBaseClassIfNeeded() {
    if (dto.getBaseDtoSimpleName() != null) {
      gc.baseClassName(dto.getBaseDtoSimpleName());
    }
  }

  private void addAnnotations() {
    gc.addAnnotation("@javax.annotation.Generated(\"dtonator\")");
    for (final String annotation : dto.getAnnotations()) {
      gc.addAnnotation(annotation);
    }
  }

  private void addInterfaces() {
    for (final String i : dto.getInterfaces()) {
      gc.implementsInterface(i);
    }
  }

  private void addDtoFields() {
    for (final DtoProperty dp : dto.getProperties()) {
      gc.getField(dp.getName()).setPublic().type(dp.getDtoType());
      if (dto.includeBeanMethods()) {
        gc.addGetterSetter(dp.getDtoType(), dp.getName());
      }
    }
  }

  private void addDefaultConstructor() {
    final GMethod cstr0 = gc.getConstructor();
    if (dto.shouldAddPublicConstructor()) {
      // keep public
      for (final DtoProperty dp : dto.getProperties()) {
        if (dp.getDtoType().startsWith("java.util.ArrayList")) {
          cstr0.body.line("this.{} = new {}();", dp.getName(), dp.getDtoType());
        }
      }
    } else {
      cstr0.setProtected();
    }
  }

  private void addFullConstructor() {
    final List<Argument> typeAndNames = list();
    for (final DtoProperty dp : dto.getAllProperties()) {
      typeAndNames.add(arg(dp.getDtoType(), dp.getName()));
    }
    final GMethod cstr = gc.getConstructor(typeAndNames);
    final List<String> superParams = list();
    for (DtoProperty dp : dto.getInheritedProperties()) {
      superParams.add(dp.getName());
    }
    cstr.body.line("super({});", Join.commaSpace(superParams));
    for (final DtoProperty dp : dto.getProperties()) {
      cstr.body.line("this.{} = {};", dp.getName(), dp.getName());
    }
  }

  private void addEqualityIfNeeded() {
    // optionally generate equals + hashCode
    final List<String> eq = dto.getEquality();
    if (eq != null) {
      gc.addEquals(eq).addHashCode(eq);
    }
  }

  private void addToString() {
    final List<String> fieldNames = list();
    if (dto.getEquality() != null) {
      fieldNames.addAll(dto.getEquality());
    } else {
      for (DtoProperty dp : dto.getAllProperties()) {
        fieldNames.add(dp.getName());
      }
    }
    gc.addToString(fieldNames);
  }

  private void createMapperTypeIfNeeded() {
    if (!dto.requiresMapperType()) {
      return;
    }
    final GClass mb = out.getClass(mapperInterface(config, dto)).setInterface();
    for (final DtoProperty p : dto.getProperties()) {
      if (!p.isExtension()) {
        continue;
      }
      final String niceName = uncapitalize(simple(dto.getDomainType()));
      // add get{propertyName}
      mb.getMethod(extensionGetter(p), arg("Mapper", "m"), arg(dto.getDomainType(), niceName)).returnType(p.getDtoType());
      if (!p.isReadOnly()) {
        // add set{propertyName}
        mb.getMethod(extensionSetter(p), arg("Mapper", "m"), arg(dto.getDomainType(), niceName), arg(p.getDtoType(), p.getName()));
      }
    }
  }

  private void addToFromMethodsToMapperIfNeeded() {
    if (dto.isManualDto()) {
      return;
    }
    addToDtoMethodToMapper();
    addToDtoOverloadToMapperIfAble();
    addFromDtoMethodToMapper();
    if (dto.hasIdProperty()) {
      addFromOnlyDtoMethodToMapper();
    }
  }

  /** Adds {@code mapper.toXxxDto(Domain)}. */
  private void addToDtoMethodToMapper() {
    final GMethod toDto = mapper.getMethod("to" + dto.getSimpleName(), arg(dto.getDomainType(), "o"));
    toDto.returnType(dto.getDtoType());
    toDto.body.line("if (o == null) {");
    toDto.body.line("_ return null;");
    toDto.body.line("}");
    for (DtoConfig subClass : dto.getSubClassDtos()) {
      toDto.body.line("if (o instanceof {}) {", subClass.getDomainType());
      toDto.body.line("_ return to{}(({}) o);", subClass.getSimpleName(), subClass.getDomainType());
      toDto.body.line("}");
    }
    toDto.body.line("return new {}(", dto.getDtoType());
    for (final DtoProperty dp : dto.getAllProperties()) {
      if (dp.isExtension()) {
        // delegate to the user's mapper method for this property
        toDto.body.line("_ {}.{}(this, o),", mapperFieldName(dp.getDto()), extensionGetter(dp));
      } else if (dp.isValueType()) {
        // delegate to the user type mapper for this property
        toDto.body.line(
          "_ o.{}() == null ? null : {}.toDto(o.{}()),",
          dp.getGetterMethodName(),
          mapperFieldName(dp.getValueTypeConfig()),
          dp.getGetterMethodName());
      } else if (dp.isEnum()) {
        // delegate to the enum converter
        toDto.body.line("_ toDto(o.{}()),", dp.getGetterMethodName());
      } else if (dp.isChainedId()) {
        toDto.body.line("_ o.{}() == null ? null : o.{}().getId(),", dp.getGetterMethodName(), dp.getGetterMethodName()); // assume getId
      } else if (dp.isEntity()) {
        // delegate to the entity's toDto converter
        toDto.body.line("_ toDto(o.{}()),", dp.getGetterMethodName());
      } else if (dp.isListOfEntities()) {
        // make and delegate to a method to convert the entities to dtos
        toDto.body.line("_ {}For{}(o.{}()),", dp.getName(), dto.getSimpleName(), dp.getGetterMethodName());
        final GMethod c = mapper.getMethod(dp.getName() + "For" + dto.getSimpleName(), arg(dp.getDomainType(), "os"));
        c.returnType(dp.getDtoType()).setPrivate();
        // assumes dto type can be instantiated
        c.body.line("if (os == null) {");
        c.body.line("_ return null;");
        c.body.line("}");
        c.body.line("{} dtos = new {}();", dp.getDtoType(), dp.getDtoType());
        c.body.line("for ({} o : os) {", dp.getSingleDomainType());
        c.body.line("_ dtos.add(to{}(o));", dp.getSimpleSingleDtoType());
        c.body.line("}");
        c.body.line("return dtos;");
      } else {
        // do a straight get
        toDto.body.line("_ o.{}(),", dp.getGetterMethodName());
      }
    }
    toDto.body.stripLastCharacterOnPreviousLine();
    toDto.body.line(");");
  }

  /** Adds {@code mapper.toDto(domain)} (no "Xxx") if the overload isn't taken yet. */
  private void addToDtoOverloadToMapperIfAble() {
    if (!takenToDtoOverloads.contains(dto.getDomainType())) {
      final GMethod toDtoOverload = mapper.getMethod("toDto", arg(dto.getDomainType(), "o"));
      toDtoOverload.returnType(dto.getDtoType());
      toDtoOverload.body.line("return to{}(o);", dto.getSimpleName());
      takenToDtoOverloads.add(dto.getDomainType());
    }
  }

  /** Adds {@code mapper.fromDto(domain, dto)}, the client is responsible for finding {@code domain}. */
  private void addFromDtoMethodToMapper() {
    final GMethod fromDto = mapper.getMethod("fromDto", //
      arg(dto.getDomainType(), "o"),
      arg(dto.getDtoType(), "dto"));
    for (final DtoProperty dp : dto.getProperties()) {
      if (dp.isReadOnly()) {
        continue;
      }
      // given we already have an instance of o, assume we shouldn't change the id
      if ("id".equals(dp.getName())) {
        continue;
      }
      if (dp.isExtension()) {
        fromDto.body.line("{}.{}(this, o, dto.{});", mapperFieldName(dto), extensionSetter(dp), dp.getName());
      } else if (dp.isValueType()) {
        fromDto.body.line(
          "o.{}(dto.{} == null ? null : {}.fromDto(dto.{}));",
          dp.getSetterMethodName(),
          dp.getName(),
          mapperFieldName(dp.getValueTypeConfig()),
          dp.getName());
      } else if (dp.isEnum()) {
        fromDto.body.line("o.{}(fromDto(dto.{}));", dp.getSetterMethodName(), dp.getName());
      } else if (dp.isChainedId()) {
        fromDto.body.line("if (dto.{} != null) {", dp.getName());
        fromDto.body.line("_ o.{}(lookup.lookup({}.class, dto.{}));", dp.getSetterMethodName(), dp.getDomainType(), dp.getName());
        fromDto.body.line("} else {");
        fromDto.body.line("_ o.{}(null);", dp.getSetterMethodName());
        fromDto.body.line("}");
      } else if (dp.isEntity()) {
        fromDto.body.line("o.{}(fromDto(dto.{}));", dp.getSetterMethodName(), dp.getName());
      } else if (dp.isListOfEntities()) {
        final String helperMethod = dp.getName() + "For" + dto.getSimpleName();
        fromDto.body.line("o.{}({}(dto.{}));", dp.getSetterMethodName(), helperMethod, dp.getName());
        final GMethod c = mapper.getMethod(helperMethod, arg(dp.getDtoType(), "dtos"));
        c.returnType(dp.getDomainType()).setPrivate();
        // assumes List->ArrayList
        c.body.line("if (dtos == null) {");
        c.body.line("_ return null;");
        c.body.line("}");
        c.body.line("{} os = new {}();", dp.getDomainType(), dp.getDomainType().replace("List", "ArrayList"));
        c.body.line("for ({} dto : dtos) {", dp.getSingleDtoType());
        c.body.line("_ os.add(fromDto(dto));");
        c.body.line("}");
        c.body.line("return os;");
      } else {
        fromDto.body.line("o.{}(dto.{});", dp.getSetterMethodName(), dp.getName());
      }
    }
    if (dto.getBaseDto() != null) {
      fromDto.body.line("fromDto(o, ({}) dto);", dto.getBaseDto().getDtoType());
    }
  }

  /** Adds {@code mapper.fromDto(dto)}, using the {@code id} and {@link DomainObjectLookup}. */
  private void addFromOnlyDtoMethodToMapper() {
    final GMethod fromDto = mapper.getMethod("fromDto", arg(dto.getDtoType(), "dto"));
    fromDto.returnType(dto.getDomainType());
    fromDto.body.line("if (dto == null) {");
    fromDto.body.line("_ return null;");
    fromDto.body.line("}");
    for (DtoConfig subClass : dto.getSubClassDtos()) {
      fromDto.body.line("if (dto instanceof {}) {", subClass.getDtoType());
      fromDto.body.line("_ return fromDto(({}) dto);", subClass.getDtoType());
      fromDto.body.line("}");
    }
    if (dto.isAbstract()) {
      fromDto.body.line("throw new IllegalArgumentException(dto + \" must be a subclass because " + dto.getDomainType() + " is abstract\");");
    } else {
      fromDto.body.line("final {} o;", dto.getDomainType());
      fromDto.body.line("if (dto.id != null) {");
      fromDto.body.line("_ o = lookup.lookup({}.class, dto.id);", dto.getDomainType());
      fromDto.body.line("} else {");
      fromDto.body.line("_ o = new {}();", dto.getDomainType());
      fromDto.body.line("}");
      fromDto.body.line("fromDto(o, dto);");
      fromDto.body.line("return o;");
    }
  }

  private static String extensionGetter(final DtoProperty p) {
    return "get" + capitalize(p.getName());
  }

  private static String extensionSetter(final DtoProperty p) {
    return "set" + capitalize(p.getName());
  }

}
