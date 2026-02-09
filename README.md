# Web application exposing the Pagila sample database

## This project as a Spring Boot application using a database

This project is used to try out *JPA/Hibernate* using a non-trivial database.

See [sample Pagila DB](https://github.com/devrimgunduz/pagila/tree/master) for initializing the database using Docker.
Make sure to start the PostgreSQL Docker container before starting the application.

The Pagila database was derived from the Sakila sample database.
See [sample Sakila DB](https://github.com/jOOQ/sakila) for the Sakila database,
as provided by Lukas Eder of [jOOQ](https://www.jooq.org/) fame. There we can find
an ER-diagram, which to a large extent explains the Pagila database as well.

Speaking of [jOOQ](https://www.jooq.org/), this project tries out jOOQ as well.
That is, the transactional services in this project have 2 implementations, one using JPA
and one using jOOQ.

Personally, while I do respect JPA/Hibernate as a very powerful database access standard API/library
for Java, I am quite impressed by *jOOQ*. Nowadays, with (immutable) Java records (as a modern
alternative to old school JavaBeans), modern SQL features, and jOOQ's type-safe and disciplined modelling
of SQL, the case for jOOQ has become quite strong. If desired, we can combine both JPA and jOOQ
in the same code base, of course.

## This project as a multi-module Maven project using Java Modules

This is also a *Maven multi-module project*. This demonstrates that large code bases using Spring Boot
can very well be organized as Maven multi-module projects, even if this project itself is a small one.

To go even further, this project also uses *Java Modules*. This enforces a chosen application architecture,
and uses the module path rather than the class path, to avoid circular dependencies across modules,
"split packages", version conflicts, etc. Typically, the Spring application uses the class path, though.
Yet if a good set of integration tests (and unit tests) run on the module path rather than the class path,
much has been achieved in benefitting from the Java Module system.

Note that it is possible to use both module path and class path together when running an application,
and sometimes this is the practical thing to do. For example, "infrastructure" code on the class path could
invoke modularized application/library code on the module path. As an aside, note that Java 25 module
imports also support that idea by offering language support for importing entire modules in a Java source
file.

In any case, the Java Module system really helps in catching several errors at an early stage.

Suppose we add a dependency on the "application" module to the POM file of the "domain" module, which
is clearly a circular dependency. If we add a corresponding "requires" statement to the "domain" module descriptor,
the Java compiler will discover and forbid this circular dependency between the 2 Java modules.
In all fairness, without modules Maven would detect this specific circular dependency too.

If we fail to "require" a dependency that is clearly needed to compile the code, the compiler will
discover and disallow this. This is also true if the dependency does occur on the module path, yet
without importing it into the module as "required" dependency. In my view, this enforcement of explicitly
"required" dependencies is an asset, not a liability.

If the compiler encounters "split packages" (so package names occurring in multiple modules), a
compilation error results. Such checks help avoid JAR conflicts that plague the classpath.
Clearly, without Java Modules the occurrence of "split packages" would be ignored, and we would easily
find ourselves back in the world of "class path hell".

These are just a few examples of (in this case only compile-time) checks by the Java Module system.
