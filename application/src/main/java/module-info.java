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
 * Application Java Module, using the service layer, and offering web access to the application.
 * <p>
 * Internals of the service layer implementation are completely encapsulated in the service layer,
 * so invisible here.
 *
 * @author Chris de Vreeze
 */
module eu.cdevreeze.pagilaapp.application {

    // Simple Java Module concerning compile-time dependencies, ignoring the Spring runtime which runs on the class path.

    requires com.google.common;
    requires org.jspecify;
    requires spring.boot;
    requires spring.context;
    requires spring.web;
    requires tools.jackson.databind;

    requires eu.cdevreeze.pagilaapp.service;

    // No need to export any packages, right?
}
