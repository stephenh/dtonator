package com.bizo.detonator;

import static joist.sourcegen.Argument.arg;
import joist.sourcegen.*;

import org.yaml.snakeyaml.Yaml;

import com.bizo.detonator.config.DtoConfig;
import com.bizo.detonator.config.DtoProperty;
import com.bizo.detonator.config.RootConfig;

public class Detonator {

  public static void main(final String args[]) {
    final Yaml y = new Yaml();
    final Object root = y.load(Detonator.class.getResourceAsStream("/detonator.yaml"));
    new Detonator(new RootConfig(root)).run();
  }

  private final RootConfig config;
  private final GDirectory out = new GDirectory("target/gen-java-src");

  static {
    GSettings.setDefaultIndentation("  ");
  }

  public Detonator(final RootConfig root) {
    config = root;
  }

  public void run() {
    final GClass mapper = out.getClass(config.getMapperPackage() + ".Mapper");
    for (final DtoConfig dto : config.getDtos()) {
      if (dto.getDomainClass().isEnum()) {
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
    for (final Object o : dto.getDomainClass().getEnumConstants()) {
      final Enum<?> e = (Enum<?>) o;
      gc.addEnumValue(e.name());
    }

    final GMethod toDto = mapper.getMethod("toDto", arg(dto.getDomainClass().getName(), "e"));
    toDto.returnType(dto.getFullName());
    toDto.body.line("switch (e) {");
    for (final Object o : dto.getDomainClass().getEnumConstants()) {
      final Enum<?> e = (Enum<?>) o;
      toDto.body.line("_ case {}: return {}.{};", e.name(), dto.getFullName(), e.name());
    }
    toDto.body.line("}");
    toDto.body.line("return null;");

    final GMethod fromDto = mapper.getMethod("fromDto", arg(dto.getFullName(), "e"));
    fromDto.returnType(dto.getFullDomainName());
    fromDto.body.line("switch (e) {");
    for (final Object o : dto.getDomainClass().getEnumConstants()) {
      final Enum<?> e = (Enum<?>) o;
      fromDto.body.line("_ case {}: return {}.{};", e.name(), dto.getFullDomainName(), e.name());
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
    for (final DtoProperty dp : dto.getDomainDescriptors()) {
      final GField f = gc.getField(dp.getName()).setPublic();
      f.type(mapDomainTypeIfNeeded(dp.getType()));
    }

    // no-arg cstr is protected
    gc.getConstructor().setProtected();

    // hack until we have getConstructors(List)
    final String[] typeAndNames = new String[dto.getDomainDescriptors().size()];
    int i = 0;
    for (final DtoProperty dp : dto.getDomainDescriptors()) {
      typeAndNames[i++] = mapDomainTypeIfNeeded(dp.getType()) + " " + dp.getName();
    }
    final GMethod cstr = gc.getConstructor(typeAndNames);
    for (final DtoProperty dp : dto.getDomainDescriptors()) {
      cstr.body.line("this.{} = {};", dp.getName(), dp.getName());
    }

    // add toDto to mapper
    final GMethod toDto = mapper.getMethod("toDto", arg(dto.getFullDomainName(), "o"));
    toDto.returnType(dto.getFullName());
    toDto.body.line("return new {}(", dto.getFullName());
    for (final DtoProperty dp : dto.getDomainDescriptors()) {
      if (dp.getGetterMethodName() == null) {
        throw new IllegalStateException("Could not find getter for " + dto.getSimpleName() + "." + dp.getName());
      }
      if (needsConversion(dp.getType())) {
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
    for (final DtoProperty dp : dto.getDomainDescriptors()) {
      if (dp.isReadOnly()) {
        throw new IllegalStateException("Could not find setter for " + dto.getSimpleName() + "." + dp.getName());
      }
      if (needsConversion(dp.getType())) {
        fromDto.body.line("o.{}(fromDto(dto.{}));", dp.getSetterMethodName(), dp.getName());
      } else {
        fromDto.body.line("o.{}(dto.{});", dp.getSetterMethodName(), dp.getName());
      }
    }
  }

  private String mapDomainTypeIfNeeded(final Class<?> domainType) {
    if (domainType.getName().startsWith(config.getDomainPackage())) {
      // in the domain package...just assume we have a dto for it?
      // should probably skip it
      // unless it's an enum
      if (domainType.isEnum()) {
        return config.getDtoPackage() + "." + domainType.getSimpleName();
      }
    }
    return domainType.getName();
  }

  private boolean needsConversion(final Class<?> domainType) {
    final String originalType = domainType.getName();
    final String mappedType = mapDomainTypeIfNeeded(domainType);
    return !originalType.equals(mappedType);
  }

}
