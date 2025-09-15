# Web application exposing the Pagila sample database

This project is used to try out JPA/Hibernate using a non-trivial database.

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
for Java, I am quite impressed by jOOQ. Nowadays, with (immutable) Java records (as a modern
alternative to old school JavaBeans), modern SQL features, and jOOQ's type-safe and disciplined modelling
of SQL, the case for jOOQ has become quite strong. If desired, we can combine both JPA and jOOQ
in the same code base, of course.
