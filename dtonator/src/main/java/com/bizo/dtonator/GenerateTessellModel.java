package com.bizo.dtonator;

import static joist.sourcegen.Argument.arg;
import static org.apache.commons.lang.StringUtils.substringAfterLast;

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

public class GenerateTessellModel {

  private static Map<String, String> propertyTypes = new HashMap<String, String>();
  private static Map<String, String> propertyInitializers = new HashMap<String, String>();
  private static final String propertyPackage = "org.tessell.model.properties";

  static {
    propertyTypes.put("java.lang.String", propertyPackage + ".StringProperty");
    propertyTypes.put("java.lang.Long", propertyPackage + ".LongProperty");
    propertyTypes.put("java.lang.Integer", propertyPackage + ".IntegerProperty");

    propertyInitializers.put("java.lang.String", "NewProperty.stringProperty");
    propertyInitializers.put("java.lang.Long", "NewProperty.longProperty");
    propertyInitializers.put("java.lang.Integer", "NewProperty.integerProperty");
  }

  private final GDirectory out;
  private final RootConfig config;
  private final DtoConfig dto;
  private final GClass baseClass;

  public GenerateTessellModel(final GDirectory out, final RootConfig config, final DtoConfig dto) {
    this.config = config;
    this.out = out;
    this.dto = dto;

    final String rootName = dto.getSimpleName().replaceAll("Dto$", "") + "Model";
    baseClass = out.getClass(config.getModelPackage() + "." + rootName + "Codegen").setAbstract().setPackagePrivate();
    baseClass.addImports("org.tessell.model.properties.NewProperty");
    baseClass.baseClassName(config.getModelBaseClass() + "<" + dto.getDtoType() + ">");
  }

  public void generate() {
    // field to hold the dto
    baseClass.getField("dto").type(dto.getDtoType());

    for (final DtoProperty p : dto.getProperties()) {
      final String innerClassName = StringUtils.capitalize(p.getName()) + "Value";

      final GField f = baseClass.getField(p.getName()).setFinal().setPublic();
      f.type(getPropertyType(p));
      f.initialValue("add({}(new {}()))", getInitializerPrefix(p), innerClassName);

      // would be nice it we could avoid this indirection and just use reflection
      // maybe AutoBean.get("foo")/AutoBean.set("foo", bar) will happen someday.
      final GClass innerValue = baseClass.getInnerClass(innerClassName).setPrivate().notStatic();
      innerValue.implementsInterface("org.tessell.model.values.Value<" + p.getDtoTypeBoxed() + ">");
      innerValue //
        .getMethod("getName")
        .returnType("String")
        .addAnnotation("@Override").body.line("return \"{}\";", p.getName());
      innerValue //
        .getMethod("set", arg(p.getDtoTypeBoxed(), "v"))
        .addAnnotation("@Override").body.line("dto.{} = v;", p.getName());
      innerValue //
        .getMethod("get")
        .returnType(p.getDtoTypeBoxed())
        .addAnnotation("@Override").body.line("return dto == null ? null : dto.{};", p.getName());
      innerValue //
        .getMethod("isReadOnly")
        .returnType("boolean")
        .addAnnotation("@Override").body.line("return {};", p.isReadOnly());
    }

    // merge
    final GMethod merge = baseClass.getMethod("merge", arg(dto.getDtoType(), "dto"));
    merge.addAnnotation("@Override");
    merge.body.line("this.dto = dto;");
    merge.body.line("all.reassessAll();");

    // getDto
    final GMethod getDto = baseClass.getMethod("getDto").returnType(dto.getDtoType());
    getDto.addAnnotation("@Override");
    getDto.body.line("return dto;");

    // cstr
    final GMethod cstr = baseClass.getConstructor(arg(dto.getDtoType(), "dto")).setProtected();
    cstr.body.line("merge(dto);"); // use setDto instead?
    cstr.body.line("addRules();");

    // addRules
    baseClass.getMethod("addRules").setProtected().setAbstract();
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

  private String getInitializerPrefix(final DtoProperty p) {
    final String type = propertyInitializers.get(p.getDtoTypeBoxed());
    if (type != null) {
      return type;
    } else if (p.isEnum()) {
      return "NewProperty.enumProperty";
    } else if (p.isList()) {
      return "NewProperty.listProperty";
    } else {
      final String customType = config.getModelPropertyType(p.getDtoType());
      if (customType != null) {
        return "new " + substringAfterLast(customType, ".");
      } else {
        return "NewProperty.basicProperty";
      }
    }
  }

}
