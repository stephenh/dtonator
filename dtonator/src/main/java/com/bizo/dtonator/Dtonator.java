package com.bizo.dtonator;

import static com.bizo.dtonator.Names.mapperInterface;
import static joist.sourcegen.Argument.arg;
import static joist.util.Copy.list;

import java.util.List;

import joist.sourcegen.GClass;
import joist.sourcegen.GDirectory;
import joist.sourcegen.GSettings;

import org.yaml.snakeyaml.Yaml;

import com.bizo.dtonator.config.DtoConfig;
import com.bizo.dtonator.config.Prune;
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
  private final GDirectory source;
  private final GDirectory out;
  private final List<String> takenToDtoOverloads = list();

  public Dtonator(final RootConfig root) {
    config = root;
    source = new GDirectory(root.getSourceDirectory());
    out = new GDirectory(root.getOutputDirectory());
    GSettings.setDefaultIndentation(root.getIndent());
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
      if (dto.includeTessellModel()) {
        new GenerateTessellModel(source, out, config, dto).generate();
      }
    }

    // create interfaces (currently abstract classes) for the value types
    for (final ValueTypeConfig vtc : config.getValueTypes()) {
      final GClass vtcg = out.getClass(mapperInterface(config, vtc)).setInterface();
      vtcg.getMethod("toDto", arg(vtc.domainType, vtc.name)).returnType(vtc.dtoType);
      vtcg.getMethod("fromDto", arg(vtc.dtoType, vtc.name)).returnType(vtc.domainType);
    }

    out.output();
    source.output();

    if (config.getPrune() == Prune.ALL_PACKAGES) {
      out.pruneIfNotTouched();
    } else if (config.getPrune() == Prune.USED_PACKAGES) {
      out.pruneIfNotTouchedWithinUsedPackages();
    }
  }
}
