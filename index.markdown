---
layout: default
title: detonator
---

Automatic DTO Generation and Mapping
------------------------------------

dtonator automates the tedious aspect of maintaining a DTO layer for exposing domain objects over the wire.

This is handy for building REST or AJAX interfaces, where the JSON or XML you send over the wire is generated for you, but not directly from your domain objects, but instead a set of custom DTOs (so that the REST/AJAX interface can be different than your domain objects).

Currently, I use dtonator for generating GWT-RPC compatible DTOs, and also generating client-side models for use in [Tessell](http://www.tessell.org); however, it is not tied to either of those technologies.

(For GWT users, dtonator, combined with a dispatch-style framework, is my take on a simpler, more straight forward [Request Factory](https://developers.google.com/web-toolkit/doc/latest/DevGuideRequestFactory).)

Screencast
----------

Here is a brief screencast of dtonator's workflow:

<p>
  <a href="http://www.tessell.org/casts/dtonator2.flv" style="display:block;width:520px;height:330px;margin-left:1em;" id="player"> </a>
  <script type="text/javascript"><!-- 
    flowplayer("player", "casts/flowplayer-3.2.7.swf", { clip: { autoPlay: false } });
  --></script>
</p>

Code Examples
-------------

For example, given a `dtonator.yaml` file like:

    config:
      dtoPackage: com.bizo.dtonator.dtos
      domainPackage: com.bizo.dtonator.domain
      mapperPackage: com.bizo.dtonator.mapper

    EmployeeDto:
      domain: Employee

dtonator will generate an `EmployeeDto` class, with all of the primitive fields in `Employee`. You can then use the generated `Mapper` to go back/forth between your domain and the DTO, like:

    public void testMapper() {
      // Mapper is generated for you
      Mapper m = new Mapper(...);
      Employee e = findSomeEmployee();
      EmployeeDto d = m.toDto(e);
      // send d over the wire...

      // later get d back over the wire
      e = m.fromDto(d);
    }

dtonator is also smart enough to map DTOs back onto the correct domain object, via the `DomainObjectLookup` interface:

    public interface DomainObjectLookup {
      <T> void lookup(Class<T> domainType, Long id);
    }

You provide dtonator with an implementation of this interface, which uses your persistence framework (Hibernate, [Joist](http://joist.ws), etc.) to find the domain objects, which dtonator will then write the DTO values back in to.

(Obviously your business logic should verify the user is accessing only the domain objects they're allowed to.)

Other Features
--------------

* Lists of child entites

  If you want to send a parent entity and it's children, you can map both entities like:

      ParentDto:
        domain: Parent
        properties: id, children

      ChildDto:
        domain: Child
        properties: id, name

  And dtonator will generate a `toDto(Parent)` and `fromDto(ParentDto)` method that recursively maps the `Child`/`ChildDto` objects via the `parent.getChildren()` and `parent.setChildren(...)` methods.

* Read only properties

  If you want to send a property to a client, but not allow them to write it:

      FooDto:
        properties: id, ~name

  The `~` marks `name` as being read-only, and it will be skipped in the `fromDto` method.

* Value type mapping

  If you have value types in your domain layer, like `java.util.Date` or Joda Time, that you can't have in your DTOs, you can map them to a separate, client-side specific value type. E.g.:

      config:
        valueTypes:
          java.util.Date: com.clientside.Date

      FooDate:
        domain: Foo
        properties: id, name, startDate

  dtonator wil generate a `DateMapper` interface that you must implement, which provides the logic to convert between `java.util.Date` and `com.clientside.Date`, and then dtonator will use that converter as needed when mapping all of the date properties in your system.

* Extension properties

  If you need properties on the DTO that are not on your domain object, you can add them:

      FooDto:
        domain: Foo
        properties: id, name, extraProperty Long

  And dtonator will generate a `FooDtoMapper` class with `getExtraProperty`/`setExtraProperty` methods for the `extraProperty` field, so you can provide the values (e.g. looking them up in an external system, or deriving them as needed). `id` and `name` will be mapped automatically as normal.

* Mapping references as ids

  It is common in DTOs to use just an id for references instead of the entire object, to reduce the data going over the wire when the client doesn't need it. For example, you might map a `Child` object as:

      ChildDto:
        domain: Child
        properties: id, name, parentId

  dtonator will treat `parentId` specially, and map it to `getParent().getId()` for setting into the DTO, and then do a `setParent(lookup.find(Parent.class, id))` for setting into the domain object. This makes mapping reference relationships trivial.

  (Note that eventually dtonator might support generic chaining of any property name, e.g. `parentName` is `getParent().getName()` and `getParent().setName(...)`, as inspired by [automapper](http://automapper.org/), but right now that is not implementated, only `xxxId` works.)

* Common interfaces

  If you need an interface, like `Serializable` or GWT's `IsSerializable` added to all of the generated DTOs, you can add a config property:

      config:
        commonInterface: java.io.Serializable

  And dtonator will add it to each DTO.

* Enum mapping

  Sometimes even enums need to be mapped into a client-side package (e.g. for GWT, which uses different packages for server-side vs. client-side code). These can be extra tedious to map. However, dtonator recognizes enums, so if you map:

      SomeEnumType:
        domain: SomeEnumType

  dtonator will create an enum `SomeEnumType` in the client package, with values for each value in the domain `SomeEnumType`, and handle mapping the values back/forth automatically.

* [Tessell](http://www.tessell.org) integration

  Although not required, since I use dtonator with Tessell, it has some extra functionality for generating client-side models. See [tessell integration](tessell-integration.html) for more information.

Community
---------

Feedback, changes, etc., feel free to contact me via github/pull requests/etc.

