package com.bizo.dtonator;

import static joist.sourcegen.Argument.arg;
import joist.sourcegen.GClass;
import joist.sourcegen.GDirectory;
import joist.sourcegen.GMethod;
import joist.sourcegen.GSettings;

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

  static {
    GSettings.setDefaultIndentation("  ");
  }

  public Dtonator(final RootConfig root) {
    config = root;
  }

  public void run() {
    final GClass mapper = out.getClass(config.getMapperPackage() + ".Mapper");
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

    final GClass gc = out.getClass(dto.getFullName()).setEnum();
    for (final String name : dto.getEnumValues()) {
      gc.addEnumValue(name);
    }

    final GMethod toDto = mapper.getMethod("toDto", arg(dto.getFullDomainName(), "e"));
    toDto.returnType(dto.getFullName());
    toDto.body.line("switch (e) {");
    for (final String name : dto.getEnumValues()) {
      toDto.body.line("_ case {}: return {}.{};", name, dto.getFullName(), name);
    }
    toDto.body.line("}");
    toDto.body.line("return null;");

    final GMethod fromDto = mapper.getMethod("fromDto", arg(dto.getFullName(), "e"));
    fromDto.returnType(dto.getFullDomainName());
    fromDto.body.line("switch (e) {");
    for (final String name : dto.getEnumValues()) {
      fromDto.body.line("_ case {}: return {}.{};", name, dto.getFullDomainName(), name);
    }
    fromDto.body.line("}");
    fromDto.body.line("return null;");
  }

  private void generateDto(final GClass mapper, final DtoConfig dto) {
    System.out.println("Generating " + dto.getSimpleName());

    final GClass gc = out.getClass(dto.getFullName());
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

    // add toDto to mapper
    final GMethod toDto = mapper.getMethod("toDto", arg(dto.getFullDomainName(), "o"));
    toDto.returnType(dto.getFullName());
    toDto.body.line("return new {}(", dto.getFullName());
    for (final DtoProperty dp : dto.getProperties()) {
      if (dp.getGetterMethodName() == null) {
        throw new IllegalStateException("Could not find getter for " + dto.getFullDomainName() + "." + dp.getName());
      }
      if (dp.needsConversion()) {
        toDto.body.line("_ toDto(o.{}()),", dp.getGetterMethodName());
      } else {
        toDto.body.line("_ o.{}(),", dp.getGetterMethodName());
      }
    }
    toDto.body.stripLastCharacterOnPreviousLine();
    toDto.body.line(");");

    // add fromDto to mapper
    final GMethod fromDto = mapper.getMethod("fromDto", //
      arg(dto.getFullDomainName(), "o"),
      arg(dto.getFullName(), "dto"));
    for (final DtoProperty dp : dto.getProperties()) {
      if (dp.isReadOnly()) {
        throw new IllegalStateException("Could not find setter for " + dto.getFullDomainName() + "." + dp.getName());
      }
      if (dp.needsConversion()) {
        fromDto.body.line("o.{}(fromDto(dto.{}));", dp.getSetterMethodName(), dp.getName());
      } else {
        fromDto.body.line("o.{}(dto.{});", dp.getSetterMethodName(), dp.getName());
      }
    }
  }

}
