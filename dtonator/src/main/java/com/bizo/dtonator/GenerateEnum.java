package com.bizo.dtonator;

import static joist.sourcegen.Argument.arg;
import joist.sourcegen.GClass;
import joist.sourcegen.GDirectory;
import joist.sourcegen.GMethod;

import com.bizo.dtonator.config.DtoConfig;

public class GenerateEnum {

  private final GClass mapper;
  private final DtoConfig dto;
  private final GClass gc;

  public GenerateEnum(final GDirectory out, final GClass mapper, final DtoConfig dto) {
    this.mapper = mapper;
    this.dto = dto;
    gc = out.getClass(dto.getDtoType()).setEnum();
  }

  public void generate() {
    System.out.println("Generating " + dto.getSimpleName());
    addAnnotations();
    addInterfaces();
    addEnumValues();
    addMapperToDto();
    addMapperFromDto();
  }

  private void addAnnotations() {
    gc.addAnnotation("@javax.annotation.Generated(\"dtonator\")");
  }

  private void addInterfaces() {
    for (final String i : dto.getInterfaces()) {
      gc.implementsInterface(i);
    }
  }

  private void addEnumValues() {
    for (final String name : dto.getEnumValues()) {
      gc.addEnumValue(name);
    }
  }

  private void addMapperToDto() {
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
  }

  private void addMapperFromDto() {
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

}
