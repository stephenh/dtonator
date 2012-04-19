
**Goal: No plumbing**

Todo
====

* A list of objects, but as the id/name
  * children: _.name
  * See BizadsCampaignDto with images and partner domains
* Use term "mirror" instead of "domain" (?)
* User types
  * Money <-> Dollars
* Generic domain object looks (`UoW.find(type, id)`)
  * Then can do `fromDto(dto)` -> `DomainObject`
  * Somehow nominate "id" as special? Just convention?
* Better syntax for read-only properties (`~id` is kind of dumb)
* Make mappers for every type, even if not needed
  * `fromDto` uses mapper `fooFromDto`, `barFromDto` with default implementations (unless extension/manual, then abstract)
* Default values for lists/etc.
* Support renaming `properties: clientName->domainName`
* Read vs. read/write children, e.g.:
  * Save `parent.children` and write back into each `child`
  * Save `parent` but use the children just for the list

Configuration
=============

* Map all properties as is:
  
      FooDto:
        domain: Foo

* Map all properties except one (skip `a`):

      FooDto:
        domain: Foo
        properties: -a, *

* Map only some properties (`a` and `b`):

      FooDto:
        domain: Foo
        properties: a, b

