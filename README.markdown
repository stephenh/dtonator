
dtonator is a code generator that generates DTOs and mapping code for sending your domain objects over the wire.

It was built for using with GWT-RPC, but it's not coupled to GWT itself.

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

Todo
====

* A list of objects, but as the id/name
  * children: `_.name` for an `ArrayList<String>`
* Better syntax for read-only properties (`~id` is kind of dumb)
* datamapper-style chaining
  * `properties: parentId -> parent.id`, read/write for `id`, otherwise read, e.g. `parentName`
  * `properties: parentIds`, use `lookup` to manage
* Move `DomainObjectLookup` to a separate `dtonator-runtime` jar

