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
 * Service layer Java Module, exposing the service layer as Java interfaces.
 *
 * @author Chris de Vreeze
 */
module eu.cdevreeze.pagilaapp.service {
    requires com.google.common;
    requires jakarta.annotation;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.jooq;
    requires org.jspecify;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.tx;

    requires transitive eu.cdevreeze.pagilaapp.domain;

    // JPA/Hibernate are internals and not exported
    // Public service API as Java interfaces
    exports eu.cdevreeze.pagilaapp.service.api;
}