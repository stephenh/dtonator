package com.bizo.dtonator;

import static com.google.common.collect.Lists.newArrayList;
import static joist.sourcegen.Argument.arg;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.uncapitalize;

import java.util.List;

import joist.sourcegen.*;

import org.yaml.snakeyaml.Yaml;

import com.bizo.dtonator.config.DtoConfig;
import com.bizo.dtonator.config.DtoProperty;
import com.bizo.dtonator.config.RootConfig;
import com.bizo.dtonator.config.UserTypeConfig;
import com.bizo.dtonator.properties.ReflectionTypeOracle;

public class Dtonator {

  public static void main(final String args[]) {
    final Yaml y = new Yaml();
    final Object root = y.load(Dtonator.class.getResourceAsStream("/dtonator.yaml"));
    new Dtonator(new RootConfig(new ReflectionTypeOracle(), root)).run();
  }

  private final RootConfig config;
  private final GDirectory out = new GDirectory("target/gen-java-src");
  private final List<String> takenToDtoOverloads = newArrayList();

  static {
    // move to config file
    GSettings.setDefaultIndentation("  ");
  }

  public Dtonator(final RootConfig root) {
    config = root;
  }

  public void run() {
    final GClass mapper = out.getClass(config.getMapperPackage() + ".Mapper");

    // create the mapper cstr
    final List<Argument> args = newArrayList();
    // always need a DomainObjectLookup
    args.add(arg(DomainObjectLookup.class.getName(), "lookup"));
    // add arguments for extension mappers
    for (final DtoConfig dto : config.getDtos()) {
      if (!dto.isManualDto() && dto.hasExtensionProperties()) {
        args.add(arg(mapperAbstractType(config, dto), mapperFieldName(dto)));
      }
    }
    // include user type mappers
    for (final UserTypeConfig utc : config.getUserTypes()) {
      args.add(arg(mapperAbstractType(config, utc), mapperFieldName(utc)));
    }
    // make fields for all of the arguments
    for (final Argument arg : args) {
      mapper.getField(arg.name).type(arg.type).setFinal();
    }
    mapper.getConstructor(args).assignFields();

    for (final DtoConfig dto : config.getDtos()) {
      if (dto.isEnum()) {
        generateEnum(mapper, dto);
      } else {
        generateDto(mapper, dto);
      }
    }

    for (final UserTypeConfig utc : config.getUserTypes()) {
      final GClass utcg = out.getClass(mapperAbstractType(config, utc)).setAbstract();
      utcg.getMethod("toDto", arg(utc.domainType, utc.name)).returnType(utc.dtoType).setAbstract();
      utcg.getMethod("fromDto", arg(utc.dtoType, utc.name)).returnType(utc.domainType).setAbstract();
    }

    out.output();
  }

  private void generateEnum(final GClass mapper, final DtoConfig dto) {
    System.out.println("Generating " + dto.getSimpleName());

    final GClass gc = out.getClass(dto.getDtoType()).setEnum();
    gc.addAnnotation("@javax.annotation.Generated(\"dtonator\")");
    for (final String name : dto.getEnumValues()) {
      gc.addEnumValue(name);
    }

    final GMethod toDto = mapper.getMethod("toDto", arg(dto.getDomainType(), "e"));
    toDto.returnType(dto.getDtoType());
    toDto.body.line("if (e == null) {");
    toDto.body.line("_ return null;");
    toDto.body.line("}");
    toDto.body.line("switch (e) {");
    for (final String name : dto.getEnumValues()) {
      toDto.body.line("_ case {}: return {}.{};", name, dto.getDtoType(), name);
    }
    toDto.body.line("}");
    toDto.body.line("return null;");

    final GMethod fromDto = mapper.getMethod("fromDto", arg(dto.getDtoType(), "e"));
    fromDto.returnType(dto.getDomainType());
    fromDto.body.line("if (e == null) {");
    fromDto.body.line("_ return null;");
    fromDto.body.line("}");
    fromDto.body.line("switch (e) {");
    for (final String name : dto.getEnumValues()) {
      fromDto.body.line("_ case {}: return {}.{};", name, dto.getDomainType(), name);
    }
    fromDto.body.line("}");
    fromDto.body.line("return null;");
  }

  private void generateDto(final GClass mapper, final DtoConfig dto) {
    System.out.println("Generating " + dto.getSimpleName());

    final GClass gc = out.getClass(dto.getDtoType());
    gc.addAnnotation("@javax.annotation.Generated(\"dtonator\")");
    // hardcoding GWT dependency for now
    gc.implementsInterface("com.google.gwt.user.client.rpc.IsSerializable");

    for (final String annotation : dto.getAnnotations()) {
      gc.addAnnotation(annotation);
    }

    // add fields for each property
    for (final DtoProperty dp : dto.getProperties()) {
      gc.getField(dp.getName()).setPublic().type(dp.getDtoType());
    }

    // no-arg cstr is protected, hardcoded for now...
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

    final List<Argument> typeAndNames = newArrayList();
    for (final DtoProperty dp : dto.getProperties()) {
      typeAndNames.add(arg(dp.getDtoType(), dp.getName()));
    }
    final GMethod cstr = gc.getConstructor(typeAndNames);
    for (final DtoProperty dp : dto.getProperties()) {
      cstr.body.line("this.{} = {};", dp.getName(), dp.getName());
    }

    if (!dto.isManualDto()) {
      // do we need a custom mapper?
      final GClass mb;
      if (dto.hasExtensionProperties()) {
        mb = out.getClass(mapperAbstractType(config, dto)).setAbstract();
        for (final DtoProperty p : dto.getProperties()) {
          if (p.isExtension()) {
            // add abstract {name}ToDto
            mb
              .getMethod(p.getName() + "ToDto", arg(dto.getDomainType(), "domain"))
              .setAbstract()
              .returnType(p.getDtoType());
            if (!p.isReadOnly()) {
              // add abstract {name}FromDto
              mb.getMethod(//
                p.getName() + "FromDto",
                arg(dto.getDomainType(), "domain"),
                arg(p.getDtoType(), "value")).setAbstract();
            }
          }
        }
      } else {
        mb = null;
      }

      // add toXxxDto to mapper
      final GMethod toDto = mapper.getMethod("to" + dto.getSimpleName(), arg(dto.getDomainType(), "o"));
      toDto.returnType(dto.getDtoType());
      toDto.body.line("return new {}(", dto.getDtoType());
      for (final DtoProperty dp : dto.getProperties()) {
        if (dp.isExtension()) {
          toDto.body.line("_ {}.{}ToDto(o),", mapperFieldName(dto), dp.getName());
        } else if (dp.isUserType()) {
          toDto.body.line(
            "_ o.{}() == null ? null : {}.toDto(o.{}()),",
            dp.getGetterMethodName(),
            mapperFieldName(dp.getUserTypeConfig()),
            dp.getGetterMethodName());
        } else if (dp.isEnum()) {
          toDto.body.line("_ toDto(o.{}()),", dp.getGetterMethodName());
        } else if (dp.isListOfEntities()) {
          toDto.body.line("_ {}For{}(o.{}()),", dp.getName(), dto.getSimpleName(), dp.getGetterMethodName());
          final GMethod c = mapper.getMethod(dp.getName() + "For" + dto.getSimpleName(), arg(dp.getDomainType(), "os"));
          c.returnType(dp.getDtoType()).setPrivate();
          // TODO assumes dto type can be instantiated
          c.body.line("{} dtos = new {}();", dp.getDtoType(), dp.getDtoType());
          c.body.line("for ({} o : os) {", dp.getSingleDomainType());
          c.body.line("_ dtos.add(to{}(o));", dp.getSimpleSingleDtoType());
          c.body.line("}");
          c.body.line("return dtos;");
        } else {
          toDto.body.line("_ o.{}(),", dp.getGetterMethodName());
        }
      }
      toDto.body.stripLastCharacterOnPreviousLine();
      toDto.body.line(");");

      // if no name classes, add an overload for toDto(Domain) to mapper
      if (!takenToDtoOverloads.contains(dto.getDomainType())) {
        final GMethod toDtoOverload = mapper.getMethod("toDto", arg(dto.getDomainType(), "o"));
        toDtoOverload.returnType(dto.getDtoType());
        toDtoOverload.body.line("return to{}(o);", dto.getSimpleName());
        takenToDtoOverloads.add(dto.getDomainType());
      }

      // add fromDto to mapper
      final GMethod fromDto = mapper.getMethod("fromDto", //
        arg(dto.getDomainType(), "o"),
        arg(dto.getDtoType(), "dto"));
      for (final DtoProperty dp : dto.getProperties()) {
        if (dp.isReadOnly()) {
          continue;
        }
        if (dp.isExtension()) {
          fromDto.body.line("{}.{}FromDto(o, dto.{});", mapperFieldName(dto), dp.getName(), dp.getName());
        } else if (dp.isUserType()) {
          fromDto.body.line(
            "o.{}(dto.{} == null ? null : {}.fromDto(dto.{}));",
            dp.getSetterMethodName(),
            dp.getName(),
            mapperFieldName(dp.getUserTypeConfig()),
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
          c.body.line("_ os.add(lookup.lookup({}.class, dto.id));", dp.getSingleDomainType());
          // TODO conditionally add a fromDto if we want to write back?
          c.body.line("}");
          c.body.line("return os;");
        } else {
          fromDto.body.line("o.{}(dto.{});", dp.getSetterMethodName(), dp.getName());
        }
      }
    }

    // optionally generate equals + hashCode
    final List<String> eq = dto.getEquality();
    if (eq != null) {
      gc.addEquals(eq).addHashCode(eq);
    }
  }

  private static String mapperFieldName(final DtoConfig dc) {
    return uncapitalize(dc.getSimpleName()) + "Mapper";
  }

  private static String mapperFieldName(final UserTypeConfig utc) {
    return utc.name + "Mapper";
  }

  private static String mapperAbstractType(final RootConfig rc, final DtoConfig dc) {
    return rc.getMapperPackage() + ".Abstract" + dc.getSimpleName() + "Mapper";
  }

  private static String mapperAbstractType(final RootConfig rc, final UserTypeConfig utc) {
    return rc.getMapperPackage() + ".Abstract" + capitalize(utc.name) + "Mapper";
  }

  private static String mapperType(final RootConfig rc, final DtoConfig dc) {
    return rc.getMapperPackage() + "." + dc.getSimpleName() + "Mapper";
  }
}
