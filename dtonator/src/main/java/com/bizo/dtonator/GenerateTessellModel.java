package com.bizo.dtonator;

import static joist.sourcegen.Argument.arg;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.removeEnd;

import java.util.HashMap;
import java.util.Map;

import joist.sourcegen.GClass;
import joist.sourcegen.GDirectory;
import joist.sourcegen.GField;
import joist.sourcegen.GMethod;

import org.apache.commons.lang.StringUtils;

import com.bizo.dtonator.config.DtoConfig;
import com.bizo.dtonator.config.DtoProperty;
import com.bizo.dtonator.config.RootConfig;

/** Generates Tessell-based {@code XxxModelCodegen}/{@code XxxModel} classes for DTOs. */
public class GenerateTessellModel {

  private static Map<String, String> propertyTypes = new HashMap<String, String>();
  private static final String propertyPackage = "org.tessell.model.properties";

  static {
    propertyTypes.put("java.lang.String", propertyPackage + ".StringProperty");
    propertyTypes.put("java.lang.Long", propertyPackage + ".LongProperty");
    propertyTypes.put("java.lang.Integer", propertyPackage + ".IntegerProperty");
    propertyTypes.put("java.lang.Boolean", propertyPackage + ".BooleanProperty");
  }

  private final RootConfig config;
  private final DtoConfig dto;
  private final GClass baseClass;

  public GenerateTessellModel(final GDirectory source, final GDirectory out, final RootConfig config, final DtoConfig dto) {
    this.config = config;
    this.dto = dto;

    // FooDto -> FooModel
    final String simpleName = dto.getSimpleName().replaceAll("Dto$", "") + "Model";
    baseClass = out.getClass(config.getModelPackage() + "." + simpleName + "Codegen").setAbstract().setPackagePrivate();
    baseClass.baseClassName(config.getModelBaseClass() + "<" + dto.getDtoType() + ">");

    // Only create FooModel if it doesn't already exist
    final String subClassName = config.getModelPackage() + "." + simpleName;
    if (!source.exists(subClassName)) {
      // Add just enough methods for it to compile and let the user go from there
      final GClass subClass = source.getClass(subClassName).baseClassName(simpleName + "Codegen");
      final GMethod cstr = subClass.getConstructor(arg(dto.getDtoType(), "dto"));
      cstr.body.line("addRules();");
      cstr.body.line("merge(dto);");
      // addRules
      subClass.getMethod("addRules").setPrivate();
    }
  }

  public void generate() {
    // field to hold the dto
    baseClass.getField("dto").type(dto.getDtoType());

    // cstr
    final GMethod cstr = baseClass.getConstructor().setProtected();

    for (final DtoProperty p : dto.getProperties()) {
      // the public final field for this XxxProperty
      final GField f = baseClass.getField(p.getName()).setFinal().setPublic().type(getPropertyType(p));

      // Until we have reflection/AutoBeans/something, we need bindgen-like inner classes to
      // wrap the dto.field reads/writes as a tessell Value that we can pass into our property.
      final String innerClassName = StringUtils.capitalize(p.getName()) + "Value";
      f.initialValue("add(new {}(new {}()))", f.getTypeClassName(), innerClassName);

      final GClass innerValue = baseClass.getInnerClass(innerClassName).setPrivate().notStatic();
      innerValue.implementsInterface("org.tessell.model.values.Value<" + p.getDtoTypeBoxed() + ">");
      // getName()
      innerValue.getMethod("getName").returnType("String").addOverride().body.line(//
        "return \"{}\";",
        p.getName());
      // set(value)
      innerValue.getMethod("set", arg(p.getDtoTypeBoxed(), "v")).addOverride().body.line(//
        "dto.{} = v;",
        p.getName());
      // get(), avoiding NPEs on read
      innerValue.getMethod("get").returnType(p.getDtoTypeBoxed()).addOverride().body.line(//
        "return dto == null ? null : dto.{};",
        p.getName());
      // isReadOnly()
      innerValue.getMethod("isReadOnly").returnType("boolean").addOverride().body.line(//
        "return {};",
        p.isReadOnly());
      // toString()
      innerValue.getMethod("toString").returnType("String").addOverride().body.line(//
        "return getName() + \" (\" + get() + \")\";");

      if (p.isListOfDtos()) {
        final DtoConfig other = p.getSingleDto();
        if (other != null && other.includeTessellModel()) {
          final String modelFieldName = removeEnd(p.getName(), "s") + "Models";
          final String dtoType = p.getSingleDtoType();
          final String modelType = other.getSimpleName().replaceAll("Dto$", "") + "Model";
          final String converterName = capitalize(p.getName()) + "Converter";

          // setup the xxxModels field
          final GField m = baseClass.getField(modelFieldName).type("ListProperty<{}>", modelType).setPublic().setFinal();
          m.initialValue("{}.as(new {}())", p.getName(), converterName);

          // add a converter back/forth
          final GClass converter = baseClass.getInnerClass(converterName).setPrivate();
          converter.implementsInterface("org.tessell.model.properties.ListProperty.ElementConverter<{}, {}>", dtoType, modelType);

          // dto -> model
          final GMethod to = converter.getMethod("to", arg(dtoType, "dto")).returnType(modelType).addAnnotation("@Override");
          to.body.line("return new {}(dto);", modelType);

          // model -> dto
          final GMethod from = converter.getMethod("from", arg(modelType, "model")).returnType(dtoType).addAnnotation("@Override");
          from.body.line("return model.getDto();");

          cstr.body.line("PropertyUtils.syncModelsToGroup(all, {});", modelFieldName);
          baseClass.addImports("org.tessell.util.PropertyUtils");
        }
      }
    }

    // merge
    final GMethod merge = baseClass.getMethod("merge", arg(dto.getDtoType(), "dto")).addOverride();
    merge.body.line("this.dto = dto;");
    merge.body.line("all.reassessAll();");

    // getDto
    final GMethod getDto = baseClass.getMethod("getDto").returnType(dto.getDtoType()).addOverride();
    getDto.body.line("return dto;");
  }

  private String getPropertyType(final DtoProperty p) {
    final String type = propertyTypes.get(p.getDtoTypeBoxed());
    if (type != null) {
      return type;
    } else if (p.isEnum()) {
      return propertyPackage + ".EnumProperty<" + p.getDtoType() + ">";
    } else if (p.isList()) {
      return propertyPackage + ".ListProperty<" + p.getSingleDtoType() + ">";
    } else {
      final String customType = config.getModelPropertyType(p.getDtoType());
      if (customType != null) {
        return customType;
      } else {
        return propertyPackage + ".BasicProperty<" + p.getDtoTypeBoxed() + ">";
      }
    }
  }

}
