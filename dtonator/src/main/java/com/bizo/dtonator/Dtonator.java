package com.bizo.dtonator;

import static com.google.common.collect.Lists.newArrayList;
import static joist.sourcegen.Argument.arg;
import static org.apache.commons.lang.StringUtils.uncapitalize;

import java.util.List;

import joist.sourcegen.*;

import org.yaml.snakeyaml.Yaml;

import com.bizo.dtonator.config.DtoConfig;
import com.bizo.dtonator.config.DtoProperty;
import com.bizo.dtonator.config.RootConfig;
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

    // add constructor for extension mappers
    final List<Argument> args = newArrayList();
    for (final DtoConfig dto : config.getDtos()) {
      if (!dto.isManualDto() && dto.hasExtensionProperties()) {
        args.add(arg(mapperAbstractType(config, dto), mapperFieldName(dto)));
      }
    }
    if (args.size() > 0) {
      for (final Argument arg : args) {
        mapper.getField(arg.name).type(arg.type).setFinal();
      }
      mapper.getConstructor(args).assignFields();
    }

    for (final DtoConfig dto : config.getDtos()) {
      if (dto.isEnum()) {
        generateEnum(mapper, dto);
      } else {
        generateDto(mapper, dto);
      }
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
    toDto.body.line("switch (e) {");
    for (final String name : dto.getEnumValues()) {
      toDto.body.line("_ case {}: return {}.{};", name, dto.getDtoType(), name);
    }
    toDto.body.line("}");
    toDto.body.line("return null;");

    final GMethod fromDto = mapper.getMethod("fromDto", arg(dto.getDtoType(), "e"));
    fromDto.returnType(dto.getDomainType());
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

    // add fields for each property
    for (final DtoProperty dp : dto.getProperties()) {
      gc.getField(dp.getName()).setPublic().type(dp.getDtoType());
    }

    // no-arg cstr is protected
    gc.getConstructor().setProtected();

    // hack until we have getConstructors(List)
    final String[] typeAndNames = new String[dto.getProperties().size()];
    int i = 0;
    for (final DtoProperty dp : dto.getProperties()) {
      typeAndNames[i++] = dp.getDtoType() + " " + dp.getName();
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
            // add abstract {name}FromDto
            mb.getMethod(//
              p.getName() + "FromDto",
              arg(dto.getDomainType(), "domain"),
              arg(p.getDtoType(), "value")).setAbstract();
            // add abstract {name}ToDto
            mb
              .getMethod(p.getName() + "ToDto", arg(dto.getDomainType(), "domain"))
              .setAbstract()
              .returnType(p.getDtoType());
          }
        }
      } else {
        mb = null;
      }

      final String toDtoName;
      if (takenToDtoOverloads.contains(dto.getDomainType())) {
        toDtoName = "to" + dto.getSimpleName();
      } else {
        toDtoName = "toDto";
        takenToDtoOverloads.add(dto.getDomainType());
      }

      // add toDto to mapper
      final GMethod toDto = mapper.getMethod(toDtoName, arg(dto.getDomainType(), "o"));
      toDto.returnType(dto.getDtoType());
      toDto.body.line("return new {}(", dto.getDtoType());
      for (final DtoProperty dp : dto.getProperties()) {
        if (dp.isExtension()) {
          toDto.body.line("{}.{}ToDto(o),", mapperFieldName(dto), dp.getName());
        } else if (dp.needsConversion()) {
          toDto.body.line("_ toDto(o.{}()),", dp.getGetterMethodName());
        } else {
          toDto.body.line("_ o.{}(),", dp.getGetterMethodName());
        }
      }
      toDto.body.stripLastCharacterOnPreviousLine();
      toDto.body.line(");");

      // add fromDto to mapper
      final GMethod fromDto = mapper.getMethod("fromDto", //
        arg(dto.getDomainType(), "o"),
        arg(dto.getDtoType(), "dto"));
      for (final DtoProperty dp : dto.getProperties()) {
        if (dp.isExtension()) {
          fromDto.body.line("{}.{}FromDto(o, dto.{});", mapperFieldName(dto), dp.getName(), dp.getName());
        } else if (dp.needsConversion()) {
          fromDto.body.line("o.{}(fromDto(dto.{}));", dp.getSetterMethodName(), dp.getName());
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

  private static String mapperAbstractType(final RootConfig rc, final DtoConfig dc) {
    return rc.getMapperPackage() + ".Abstract" + dc.getSimpleName() + "Mapper";
  }

  private static String mapperType(final RootConfig rc, final DtoConfig dc) {
    return rc.getMapperPackage() + "." + dc.getSimpleName() + "Mapper";
  }
}
