package com.bizo.dtonator;

import static com.bizo.dtonator.Names.mapperAbstractType;
import static com.bizo.dtonator.Names.mapperFieldName;
import static com.google.common.collect.Lists.newArrayList;
import static joist.sourcegen.Argument.arg;

import java.util.List;

import joist.sourcegen.Argument;
import joist.sourcegen.GClass;
import joist.sourcegen.GDirectory;
import joist.sourcegen.GMethod;

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

  public GenerateDto(
      final RootConfig config,
      final GDirectory out,
      final GClass mapper,
      final List<String> takenToDtoOverloads,
      final DtoConfig dto) {
    this.config = config;
    this.out = out;
    this.mapper = mapper;
    this.takenToDtoOverloads = takenToDtoOverloads;
    this.dto = dto;
    gc = out.getClass(dto.getDtoType());
  }

  public void generate() {
    System.out.println("Generating " + dto.getSimpleName());
    addAnnotations();
    addInterfaces();
    addDtoFields();
    addDefaultConstructor();
    addFullConstructor();
    addEqualityIfNeeded();
    addToFromMethodsToMapperIfNeeded();
    createMapperTypeIfNeeded();
  }

  private void addAnnotations() {
    gc.addAnnotation("@javax.annotation.Generated(\"dtonator\")");
    for (final String annotation : dto.getAnnotations()) {
      gc.addAnnotation(annotation);
    }
  }

  private void addInterfaces() {
    // hardcoding GWT dependency for now
    gc.implementsInterface("com.google.gwt.user.client.rpc.IsSerializable");
  }

  private void addDtoFields() {
    for (final DtoProperty dp : dto.getProperties()) {
      gc.getField(dp.getName()).setPublic().type(dp.getDtoType());
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
    final List<Argument> typeAndNames = newArrayList();
    for (final DtoProperty dp : dto.getProperties()) {
      typeAndNames.add(arg(dp.getDtoType(), dp.getName()));
    }
    final GMethod cstr = gc.getConstructor(typeAndNames);
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

  private void createMapperTypeIfNeeded() {
    if (dto.isManualDto() || !dto.hasExtensionProperties()) {
      return;
    }
    final GClass mb = out.getClass(mapperAbstractType(config, dto)).setAbstract();
    for (final DtoProperty p : dto.getProperties()) {
      if (!p.isExtension()) {
        continue;
      }
      // add abstract {propertyName}ToDto
      mb.getMethod(//
        p.getName() + "ToDto",
        arg(dto.getDomainType(), "domain")).setAbstract().returnType(p.getDtoType());
      if (!p.isReadOnly()) {
        // add abstract {propertyName}FromDto
        mb.getMethod(//
          p.getName() + "FromDto",
          arg(dto.getDomainType(), "domain"),
          arg(p.getDtoType(), "value")).setAbstract();
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
    addFromOnlyDtoMethodToMapper();
  }

  /** Adds {@code mapper.toXxxDto(Domain)}. */
  private void addToDtoMethodToMapper() {
    final GMethod toDto = mapper.getMethod("to" + dto.getSimpleName(), arg(dto.getDomainType(), "o"));
    toDto.returnType(dto.getDtoType());
    toDto.body.line("return new {}(", dto.getDtoType());
    for (final DtoProperty dp : dto.getProperties()) {
      if (dp.isExtension()) {
        // delegate to the user's mapper method for this property
        toDto.body.line("_ {}.{}ToDto(o),", mapperFieldName(dto), dp.getName());
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
      } else if (dp.isListOfEntities()) {
        // make and delegate to a method to convert the entities to dtos
        toDto.body.line("_ {}For{}(o.{}()),", dp.getName(), dto.getSimpleName(), dp.getGetterMethodName());
        final GMethod c = mapper.getMethod(dp.getName() + "For" + dto.getSimpleName(), arg(dp.getDomainType(), "os"));
        c.returnType(dp.getDtoType()).setPrivate();
        // assumes dto type can be instantiated
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
        fromDto.body.line("{}.{}FromDto(o, dto.{});", mapperFieldName(dto), dp.getName(), dp.getName());
      } else if (dp.isValueType()) {
        fromDto.body.line(
          "o.{}(dto.{} == null ? null : {}.fromDto(dto.{}));",
          dp.getSetterMethodName(),
          dp.getName(),
          mapperFieldName(dp.getValueTypeConfig()),
          dp.getName());
      } else if (dp.isEnum()) {
        fromDto.body.line("o.{}(fromDto(dto.{}));", dp.getSetterMethodName(), dp.getName());
      } else if (dp.isListOfEntities()) {
        final String helperMethod = dp.getName() + "For" + dto.getSimpleName();
        fromDto.body.line("o.{}({}(dto.{}));", dp.getSetterMethodName(), helperMethod, dp.getName());
        final GMethod c = mapper.getMethod(helperMethod, arg(dp.getDtoType(), "dtos"));
        c.returnType(dp.getDomainType()).setPrivate();
        // assumes List->ArrayList
        c.body.line("{} os = new {}();", dp.getDomainType(), dp.getDomainType().replace("List", "ArrayList"));
        c.body.line("for ({} dto : dtos) {", dp.getSingleDtoType());
        // assumes dto.id is the key
        c.body.line("_ if (dto.id != null) {");
        c.body.line("_ _ os.add(lookup.lookup({}.class, dto.id));", dp.getSingleDomainType());
        c.body.line("_ }");
        // TODO conditionally add a fromDto if we want to write back?
        c.body.line("}");
        c.body.line("return os;");
      } else {
        fromDto.body.line("o.{}(dto.{});", dp.getSetterMethodName(), dp.getName());
      }
    }
  }

  /** Adds {@code mapper.fromDto(dto)}, using the {@code id} and {@link DomainObjectLookup}. */
  private void addFromOnlyDtoMethodToMapper() {
    final GMethod fromDto = mapper.getMethod("fromDto", arg(dto.getDtoType(), "dto"));
    fromDto.returnType(dto.getDomainType());
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
