/*
 * Copyright 2025-2025 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Root package of the application.
 * <p>
 * Note how this application beautifully combines the best of immutability and mutability, limiting
 * the scope of the latter. That is, the service layer API contract has methods taking and returning
 * immutable data, such as immutable Java records and immutable Guava collections, while the service
 * layer implementation classes use mutable JPA entities to access the PostgreSQL database.
 * <p>
 * Note that the test code (especially integration tests) have been set up in such a way that they
 * interfere as little as possible with the running application. That is, these tests use Testcontainers
 * (for PostgreSQL), but care has been taken to prevent port conflicts with the running application
 * (for web container port and database engine port).
 *
 * @author Chris de Vreeze
 */
@NullMarked
package eu.cdevreeze.pagilaapp;

import org.jspecify.annotations.NullMarked;
