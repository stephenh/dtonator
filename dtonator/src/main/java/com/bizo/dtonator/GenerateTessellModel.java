package com.bizo.dtonator;

import static joist.sourcegen.Argument.arg;

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
      subClass.getConstructor(arg(dto.getDtoType(), "dto")).body.line("super(dto);");
      subClass.getMethod("addRules").addAnnotation("@Override").setProtected();
    }
  }

  public void generate() {
    // field to hold the dto
    baseClass.getField("dto").type(dto.getDtoType());

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
      innerValue.getMethod("getName").returnType("String").addAnnotation("@Override").body.line("return \"{}\";", p.getName());
      // set(value)
      innerValue.getMethod("set", arg(p.getDtoTypeBoxed(), "v")).addAnnotation("@Override").body.line("dto.{} = v;", p.getName());
      // get(), avoiding NPEs on read
      innerValue.getMethod("get").returnType(p.getDtoTypeBoxed()).addAnnotation("@Override").body.line(
        "return dto == null ? null : dto.{};",
        p.getName());
      // isReadOnly()
      innerValue.getMethod("isReadOnly").returnType("boolean").addAnnotation("@Override").body.line("return {};", p.isReadOnly());
      // toString()
      innerValue.getMethod("toString").returnType("String").addAnnotation("@Override").body.line("return getName() + \" (\" + get() + \")\";");
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

}
