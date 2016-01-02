
dtonator is a code generator that generates DTOs and mapping code for sending your domain objects over the wire.

It's differentiating feature (from alternatives like [dozer](http://dozer.sourceforge.net/)) is that it doesn't use any runtime reflection, and instead generates code at build-time to do all of the mappings. This is actually less for performance (which is usually the goal for avoiding reflection, at least historically) and primarily for simplicity. You can look/debug through the generated code to see exactly what's happening in the mapping logic. It's all very straightforward.

It was built for using with GWT-RPC, but it's not coupled to GWT itself.

Download
========

dtonator artifacts are available at the [repo.joist.ws](http://repo.joist.ws) Maven repository, with org `com.bizo` and module `dtonator`.

Configuration
=============

dtonator uses a `dtonator.yaml` file to configure what it generates.

A sample configuration file looks like:

```yaml
config:
  dtoPackage: com.bizo.dtonator.dtos
  domainPackage: com.bizo.dtonator.domain
  mapperPackage: com.bizo.dtonator.mapper

EmployeeDto:
  domain: Employee
```

Given this dtonator will generate an `EmployeeDto` with all of the primitive properties of `Employee` (discovered via reflection) and a `Mapper` class that gets/sets the properties. You would use the result like:

```java
// Mapper is generated
Mapper mapper = new Mapper(...);

// dto -> domain
// EmployeeDto is generated
EmployeeDto dto = new EmployeeDto(1,  "ee1");
// sets the DTO values back into Employee
Employee ee = mapper.fromDto(dto);

// domain -> dto
EmployeeDto dto = mapper.toDto(ee);
```

Besides simple mappings where the names and types match, dtonator supports a number of cases that come up when mapping DTOs.

* Mapping all basic (non-entity/non-list) properties is the default behavior:

    ```yaml
    FooDto:
      domain: Foo
    ```

* Map all properties except one (skip `a`, include the rest `*`):

    ```yaml
    FooDto:
      domain: Foo
      properties: -a, *
    ```

* Map only certain properties (`a` and `b`):

    ```yaml
    FooDto:
      domain: Foo
      properties: a, b
    ```

* Map extra properties that aren't on the domain object

    ```yaml
    FooDto:
      domain: Foo
      properties: a, newProperty String
    ```

  For dtonator to get/set the value of this unknown `newProperty`, it generates an interface, `FooDtoMapper`, which you must implement to provide the `newProperty` semantics:

    ```java
    public interface FooDtoMapper {
      String getNewProperty(Foo foo);

      void setNewProperty(Foo foo, String newProperty);
    }
    ```

* Include a list of child objects:

    ```yaml
    EmployerDto:
      domain: Employer
      properties: employees

    EmployeeDto:
      domain: Employee
    ```

  The usage would look like:

    ```java
    EmployerDto erDto = new EmployerDto(
      1l,
      newArrayList(new EmployeeDto(1l))
    ```

  By default dtonator will use the `DomainObjectLookup` to look up each `Employee` object and call `employer.setEmployees(theEmployees)`. 

* Aliases properties to different names

    ```yaml
    EmployeeDto:
      domain: Employee
      properties: shortName(longNameOnDomainObject)
    ```

* Generate getters/setters methods:

    ```yaml
    EmployeeDto:
      properties: id Long, name String
      beanMethods: true
    ```

  The generated `EmployeeDto` will have `getName` and `setName` methods.

  Bean methods can also be configured globally for all DTOs.

* With getters/setters, DTOs can also implement interfaces, e.g.:

    ```java
    interface HasName {
      String getName();
    }
    ```

    ```yaml
    EmployeeDto:
      properties: id Long, name String
      interfaces: com.foo.HasName
      beanMethods: true
    ```

   The generated `EmployeeDto` will implement `HasName`.

Other Configuration Options
===========================

* Output directory (defaults to `target/gen-java-src`)

    ```yaml
    config:
      outputDirectory: target/java
    ```

* Indentation of the generated code (defaults to four space)

    ```yaml
    config:
      indent: two-space | tab | four-space
    ```

* Interfaces to be added to all DTOs/enums:

    ```yaml
    config:
      commonInterface: java.io.Serializable
    ```

Integrating with your Build Environment
=======================================

dtonator is a pre-compilation code generation, e.g. you should invoke it in your build process before calling `javac`.

It can also be setup in IDEs to run "on save".

* In ant, use an "exec" task with `com.bizo.dtonator.Dtonator` as the main class, and the dtonator jar on the classpath
* In Eclipse, you can setup an [External Tool Builder](http://www.ibm.com/developerworks/opensource/tutorials/os-eclipse-tools/) to invoke the `java` system command with `com.bizo.dtonator.Dtonator` as the main class, and a `-cp` argument of the dtonator jar + your config file. You can set it up to run automatically with the "Build Options" tab when configuring the builder. For example, something like:
  * Location: `${system_path:java}`
  * Working Directory: `${workspace_loc:/fooproject-web}`
  * Arguments: `-cp "../fooproject-domain/target/classes:src/main/webapp/WEB-INF/classes:lib/eclipse/*" com.bizo.dtonator.Dtonator`
* In IntelliJ, you can probably use something like the [File Watchers plugin](https://www.jetbrains.com/idea/help/file-watchers.html) to watch for changes to the input `.class` files or the `dtonator.yaml` config file, similar to the Eclipse External Tool Builder setup.
* In gradle or Maven, you should be able to translate the ant "exec" task into a respective pre-compilation task/goal/etc.

Assumptions about Build Order
=============================

Because dtonator is a pre-compilation code generator, any information it gains via reflection (e.g. automatically inferring the types of properties on mapped objects) must already be available/compiled in `.class` files. This means if you want to generate DTOs for your domain objects, you would need a build flow like:

1. Compile your domain objects into `.class` files
2. Run dtonator with `dtonator.yaml` + domain object `.class` files on the classpath
  * This creates various `Mapper.java`/etc. output files
3. Compile your webapp/API code + generated `Mapper.java`/etc. together

Depending on your project setup, this might best be achieved by having your domain objects be a separate project (so a separate Maven/gradle/etc.) build than your webapp/API layer.

Building dtonator itself
========================

For Eclipse:

* Install [IvyDE](http://ant.apache.org/ivy/ivyde/)
* Ensure Preferences / Ivy / Classpath Container / Resolve dependencies in workspace is checked
* Import the `dtonator/.project` and `features/.project`
* Run `dtonator-features.launch` to update the output for the `dtonator-features` project for testing

For command line:

* Install [buildr](http://buildr.apache.org/a)
* Install [ivy4r](https://github.com/klaas1979/ivy4r)
* To package:
  * `cd dtonator`
  * `version=x.y buildr package`

Todo
====

* A list of objects, but as the id/name
  * children: `_.name` for an `ArrayList<String>`
* Better syntax for read-only properties (`~id` is kind of dumb)
* datamapper-style chaining
  * `properties: parentId -> parent.id`, read/write for `id`, otherwise read, e.g. `parentName`
  * `properties: parentIds`, use `lookup` to manage
* Move `DomainObjectLookup` to a separate `dtonator-runtime` jar

