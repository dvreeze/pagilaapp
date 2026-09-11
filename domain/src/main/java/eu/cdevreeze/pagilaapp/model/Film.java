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

import module java.base;
import com.google.common.collect.ImmutableSet;
import org.jspecify.annotations.Nullable;

/**
 * Immutable film record. The ID, if any, is the technical primary key.
 *
 * @author Chris de Vreeze
 */
public record Film(
        @Nullable Integer id,
        String title,
        @Nullable String description,
        @Nullable Year releaseYear,
        String language,
        @Nullable String originalLanguage,
        ImmutableSet<Category> categories,
        ImmutableSet<Actor> actors,
        short rentalDuration,
        BigDecimal rentalRate,
        @Nullable Integer length,
        BigDecimal replacementCost,
        @Nullable String rating,
        @Nullable ImmutableSet<String> specialFeatures
) {

    public OptionalInt idOption() {
        return id == null ? OptionalInt.empty() : OptionalInt.of(id);
    }

    public Optional<String> descriptionOption() {
        return Optional.ofNullable(description);
    }

    public Optional<Year> releaseYearOption() {
        return Optional.ofNullable(releaseYear);
    }

    public Optional<String> originalLanguageOption() {
        return Optional.ofNullable(originalLanguage);
    }

    public OptionalInt lengthOption() {
        return length == null ? OptionalInt.empty() : OptionalInt.of(length);
    }

    public Optional<String> ratingOption() {
        return Optional.ofNullable(rating);
    }

    public Optional<ImmutableSet<String>> specialFeaturesOption() {
        return Optional.ofNullable(specialFeatures);
    }

    public ImmutableSet<String> actorNames() {
        return actors().stream().map(Actor::name).collect(ImmutableSet.toImmutableSet());
    }

    public Film withActors(ImmutableSet<Actor> actors) {
        return new Film(
                id(),
                title(),
                description(),
                releaseYear(),
                language(),
                originalLanguage(),
                categories(),
                actors,
                rentalDuration(),
                rentalRate(),
                length(),
                replacementCost(),
                rating(),
                specialFeatures()
        );
    }

    public Film withCategories(ImmutableSet<Category> categories) {
        return new Film(
                id(),
                title(),
                description(),
                releaseYear(),
                language(),
                originalLanguage(),
                categories,
                actors(),
                rentalDuration(),
                rentalRate(),
                length(),
                replacementCost(),
                rating(),
                specialFeatures()
        );
    }
}
