
dtonator is a code generator that will generate DTOs and mapping code for sending your domain objects over the wire.

dtonator was built for sending DTOs to GWT clients via GWT-RPC, but it's (...eventually) not coupled to GWT.

Configuration
=============

dtonator uses a `dtonator.yaml` file to configure what it generates.

A sample configuration file looks like:

    config:
      dtoPackage: com.bizo.dtonator.dtos
      domainPackage: com.bizo.dtonator.domain
      mapperPackage: com.bizo.dtonator.mapper

    EmployeeDto:
      domain: Employee

Given this dtonator will create an `EmployeeDto` with all of the primitive properties of `Employee` and a `Mapper` class that gets/sets the properties. You could use it like:

    Mapper mapper = new Mapper(...);

    // dto -> domain
    Employee ee = mapper.fromDto(employeeDto);

    // domain -> dto
    EmployeeDto dto = mapper.toDto(ee);

Besides simple mappings where the names and types match, dtonator supports a number of boundary cases that come up when mapping DTOs.

* Map all properties as is:
  
      FooDto:
        domain: Foo

* Map all properties except one (skip `a`):

      FooDto:
        domain: Foo
        properties: -a, *

* Map only explicitly listed properties (`a` and `b`):

      FooDto:
        domain: Foo
        properties: a, b

* Include a list of child objects:

      EmployerDto:
        domain: Employer
        properties: employees

      EmployeeDto:
        domain: Employee

  The usage would look like:

      EmployerDto erDto = new EmployerDto(
        1l,
        newArrayList(new EmployeeDto(1l))
  By default dtonator will use the `DomainObjectLookup` to look up each `Employee` object and call `employer.setEmployees(theEmployees)`. 

Todo
====

* A list of objects, but as the id/name
  * children: _.name
  * See BizadsCampaignDto with images and partner domains
* Use term "mirror" instead of "domain" (?)
* Better syntax for read-only properties (`~id` is kind of dumb)
* Support renaming `properties: clientName->domainName`
* Read vs. read/write children, e.g.:
  * Save `parent.children` and write back the properties for each `child`
  * Save `parent` but use the children just for the list (current behavior)
* datamapper-style chaining
  * `properties: parentId -> parent.id`, read/write for `id`, otherwise read, e.g. `parentName`
  * `properties: parentIds`, use `lookup` to manage

