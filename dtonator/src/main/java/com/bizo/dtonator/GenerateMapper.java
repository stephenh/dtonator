package com.bizo.dtonator;

import static com.bizo.dtonator.Names.mapperAbstractType;
import static com.bizo.dtonator.Names.mapperFieldName;
import static com.bizo.dtonator.Names.mapperInterface;
import static com.google.common.collect.Lists.newArrayList;
import static joist.sourcegen.Argument.arg;

import java.util.List;

import joist.sourcegen.Argument;
import joist.sourcegen.GClass;
import joist.sourcegen.GDirectory;

import com.bizo.dtonator.config.DtoConfig;
import com.bizo.dtonator.config.RootConfig;
import com.bizo.dtonator.config.ValueTypeConfig;

public class GenerateMapper {

  private final RootConfig config;
  private final GClass mapper;

  public GenerateMapper(final GDirectory out, final RootConfig config) {
    this.config = config;
    mapper = out.getClass(config.getMapperPackage() + ".Mapper");
  }

  public void generate() {
    addConstructorAndFields();
  }

  public GClass getMapper() {
    return mapper;
  }

  private void addConstructorAndFields() {
    // create the mapper cstr
    final List<Argument> args = newArrayList();
    // we always need a DomainObjectLookup
    args.add(arg(DomainObjectLookup.class.getName(), "lookup"));
    // add arguments for extension mappers, if any
    for (final DtoConfig dto : config.getDtos()) {
      if (!dto.isManualDto() && dto.hasExtensionProperties()) {
        args.add(arg(mapperAbstractType(config, dto), mapperFieldName(dto)));
      }
    }
    // include user type mappers
    for (final ValueTypeConfig utc : config.getValueTypes()) {
      args.add(arg(mapperInterface(config, utc), mapperFieldName(utc)));
    }
    // make fields for all of the arguments
    for (final Argument arg : args) {
      mapper.getField(arg.name).type(arg.type).setFinal();
    }
    mapper.getConstructor(args).assignFields();
  }

}
