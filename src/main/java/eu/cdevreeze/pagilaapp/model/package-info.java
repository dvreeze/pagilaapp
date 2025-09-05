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
 * Domain as immutable data classes, using immutable Guava collections for collection-valued data.
 * <p>
 * These classes are immutable record classes, so they have well-defined value equality.
 * Also, with these data classes what you see is what you get: no lazy loading, no proxying, and no
 * hidden state.
 * <p>
 * These immutable data classes help make the abstract service API completely technology-agnostic,
 * which also enhanced testability of higher layers than the service layer.
 *
 * @author Chris de Vreeze
 */
@NullMarked
package eu.cdevreeze.pagilaapp.model;

import org.jspecify.annotations.NullMarked;
