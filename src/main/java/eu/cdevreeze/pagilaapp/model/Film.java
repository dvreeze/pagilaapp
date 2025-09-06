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

package eu.cdevreeze.pagilaapp.model;

import com.google.common.collect.ImmutableSet;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Immutable film record. The ID, if any, is the technical primary key.
 *
 * @author Chris de Vreeze
 */
public record Film(
        OptionalInt idOption,
        String title,
        Optional<String> descriptionOption,
        Optional<Year> releaseYearOption,
        String language,
        Optional<String> originalLanguageOption,
        ImmutableSet<Category> categories,
        ImmutableSet<Actor> actors,
        short rentalDuration,
        BigDecimal rentalRate,
        OptionalInt lengthOption,
        BigDecimal replacementCost,
        Optional<String> ratingOption,
        Optional<ImmutableSet<String>> specialFeaturesOption
) {
}
