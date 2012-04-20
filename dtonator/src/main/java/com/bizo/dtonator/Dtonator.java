package com.bizo.dtonator;

import static com.bizo.dtonator.Names.mapperAbstractType;
import static com.google.common.collect.Lists.newArrayList;
import static joist.sourcegen.Argument.arg;

import java.util.List;

import joist.sourcegen.GClass;
import joist.sourcegen.GDirectory;
import joist.sourcegen.GSettings;

import org.yaml.snakeyaml.Yaml;

import com.bizo.dtonator.config.DtoConfig;
import com.bizo.dtonator.config.RootConfig;
import com.bizo.dtonator.config.ValueTypeConfig;
import com.bizo.dtonator.properties.ReflectionTypeOracle;

/**
 * Runs the dtonator code generation process.
 * 
 * Loads a {@code /dtonator.yaml} file from the classpath, parses it, then generates DTOs to {@code target/gen-java-src}
 * . Both are currently hard coded.
 */
public class Dtonator {

  public static void main(final String args[]) {
    final Object root = new Yaml().load(Dtonator.class.getResourceAsStream("/dtonator.yaml"));
    new Dtonator(new RootConfig(new ReflectionTypeOracle(), root)).run();
  }

  private final RootConfig config;
  private final GDirectory out = new GDirectory("target/gen-java-src");
  private final List<String> takenToDtoOverloads = newArrayList();

  public Dtonator(final RootConfig root) {
    config = root;
    // move to config file
    GSettings.setDefaultIndentation("  ");
  }

  public void run() {
    final GenerateMapper gm = new GenerateMapper(out, config);
    gm.generate();
    final GClass mapper = gm.getMapper();

    for (final DtoConfig dto : config.getDtos()) {
      if (dto.isEnum()) {
        new GenerateEnum(out, mapper, dto).generate();
      } else {
        new GenerateDto(config, out, mapper, takenToDtoOverloads, dto).generate();
      }
    }

    // create interfaces (currently abstract classes) for the user type mappers
    for (final ValueTypeConfig vtc : config.getValueTypes()) {
      final GClass vtcg = out.getClass(mapperAbstractType(config, vtc)).setAbstract();
      vtcg.getMethod("toDto", arg(vtc.domainType, vtc.name)).returnType(vtc.dtoType).setAbstract();
      vtcg.getMethod("fromDto", arg(vtc.dtoType, vtc.name)).returnType(vtc.domainType).setAbstract();
    }

    out.output();
  }
}
