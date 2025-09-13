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

package eu.cdevreeze.pagilaapp.service.jooqimpl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import eu.cdevreeze.pagilaapp.jooq.enums.MpaaRating;
import eu.cdevreeze.pagilaapp.jooq.tables.Language;
import eu.cdevreeze.pagilaapp.model.Actor;
import eu.cdevreeze.pagilaapp.model.Category;
import eu.cdevreeze.pagilaapp.model.Film;
import eu.cdevreeze.pagilaapp.service.FilmService;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static eu.cdevreeze.pagilaapp.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

/**
 * jOOQ FilmService implementation.
 * <p>
 * See <a href="https://blog.jooq.org/create-empty-optional-sql-clauses-with-jooq/">create empty optional SQL clauses with jOOQ</a>
 * for the approach followed to reuse queries in a way that works well in jOOQ.
 *
 * @author Chris de Vreeze
 */
@Service
@ConditionalOnBooleanProperty(name = "useJooq")
public class JooqFilmService implements FilmService {

    // The type-safety offered by jOOQ is absolutely great
    // We just write SQL, be it in Java, checked by the Java compiler
    // On the other hand, the jOOQ Record types become a bit unwieldy with more than a few columns
    // Moreover, we lose type-safe with over 22 columns
    // Yet we can combine columns into a "row" to mitigate this, should we need explicit jOOQ Record (sub)types

    private record CategoryRow(@Nullable Integer id, String name) {

        public Category toModel() {
            return new Category(
                    Optional.ofNullable(id).stream().mapToInt(i -> i).findFirst(),
                    name
            );
        }
    }

    private record ActorRow(
            @Nullable Integer id,
            String firstName,
            String lastName
    ) {

        public Actor toModel() {
            return new Actor(
                    Optional.ofNullable(id).stream().mapToInt(i -> i).findFirst(),
                    firstName,
                    lastName
            );
        }
    }

    private record FilmRow(
            @Nullable Integer id,
            String title,
            @Nullable String description,
            @Nullable Integer releaseYear,
            String language,
            @Nullable String originalLanguage,
            List<CategoryRow> categories,
            List<ActorRow> actors,
            short rentalDuration,
            BigDecimal rentalRate,
            @Nullable Short length,
            BigDecimal replacementCost,
            @Nullable MpaaRating rating,
            @Nullable String[] specialFeatures
    ) {

        public Film toModel() {
            return new Film(
                    Optional.ofNullable(id).stream().mapToInt(i -> i).findFirst(),
                    title,
                    Optional.ofNullable(description),
                    Optional.ofNullable(releaseYear).map(Year::of),
                    language,
                    Optional.ofNullable(originalLanguage),
                    categories.stream().map(CategoryRow::toModel).collect(ImmutableSet.toImmutableSet()),
                    actors.stream().map(ActorRow::toModel).collect(ImmutableSet.toImmutableSet()),
                    rentalDuration,
                    rentalRate,
                    Optional.ofNullable(length).stream().mapToInt(i -> i).findFirst(),
                    replacementCost,
                    Optional.ofNullable(rating).map(MpaaRating::toString),
                    Optional.of(specialFeatures).map(Arrays::asList).filter(v -> !v.isEmpty()).map(ImmutableSet::copyOf)
            );
        }
    }

    private final DSLContext dsl;

    public JooqFilmService(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findAllFilms() {
        return findFilms(DSL.noCondition());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findFilmsByLanguage(String language) {
        // Note that we do not have to trim the fixed length "LANGUAGE.NAME" column
        // That's because we configured jOOQ to trim those columns

        return findFilms(
                upper(LANGUAGE.NAME).eq(language.toUpperCase())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findFilmsByCategory(String category) {
        return findFilmsByCategories(ImmutableSet.of(category));
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findFilmsByCategories(ImmutableSet<String> categories) {
        return findFilms(
                upper(CATEGORY.NAME).in(categories.stream().map(String::toUpperCase).toList())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findFilmsByActor(String firstName, String lastName) {
        return findFilms(
                upper(ACTOR.FIRST_NAME).eq(firstName.toUpperCase())
                        .and(upper(ACTOR.LAST_NAME).eq(lastName.toUpperCase()))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableSet<String> findAllFilmCategories() {
        return dsl.select(CATEGORY.CATEGORY_ID, CATEGORY.NAME)
                .from(CATEGORY)
                .fetchStream()
                .map(Records.mapping(CategoryRow::new))
                .distinct()
                .filter(Objects::nonNull)
                .map(CategoryRow::name)
                .collect(ImmutableSet.toImmutableSet());
    }

    private ImmutableList<Film> findFilms(Condition whereCondition) {
        // Creating an alias for the Language table, to be used for the original language join
        Language originalLanguage = LANGUAGE.as("ORIGINAL_LANGUAGE");

        // Note that in theory we do not have to trim the fixed length "LANGUAGE.NAME" column
        // That's because we configured jOOQ to trim those columns
        // That works inside "where" clauses, but I do not see such trimming in the "select" clause

        // TODO Improve: not all extra joins are needed all the time!

        return dsl
                .selectDistinct(
                        FILM.FILM_ID,
                        FILM.TITLE,
                        FILM.DESCRIPTION,
                        FILM.RELEASE_YEAR,
                        rtrim(LANGUAGE.NAME),
                        rtrim(originalLanguage.NAME),
                        multiset(
                                select(
                                        CATEGORY.CATEGORY_ID,
                                        CATEGORY.NAME
                                ).from(CATEGORY)
                                        .join(FILM_CATEGORY)
                                        .on(CATEGORY.CATEGORY_ID.eq(FILM_CATEGORY.CATEGORY_ID))
                                        .where(FILM_CATEGORY.FILM_ID.eq(FILM.FILM_ID))
                        ).convertFrom(r -> r.map(Records.mapping(CategoryRow::new))),
                        multiset(
                                select(
                                        ACTOR.ACTOR_ID,
                                        ACTOR.FIRST_NAME,
                                        ACTOR.LAST_NAME
                                )
                                        .from(ACTOR)
                                        .join(FILM_ACTOR)
                                        .on(ACTOR.ACTOR_ID.eq(FILM_ACTOR.ACTOR_ID))
                                        .where(FILM_ACTOR.FILM_ID.eq(FILM.FILM_ID))
                        ).convertFrom(r -> r.map(Records.mapping(ActorRow::new))),
                        FILM.RENTAL_DURATION,
                        FILM.RENTAL_RATE,
                        FILM.LENGTH,
                        FILM.REPLACEMENT_COST,
                        FILM.RATING,
                        FILM.SPECIAL_FEATURES
                )
                .from(FILM)
                .leftJoin(LANGUAGE)
                .on(FILM.LANGUAGE_ID.eq(LANGUAGE.LANGUAGE_ID))
                .leftJoin(originalLanguage)
                .on(FILM.ORIGINAL_LANGUAGE_ID.eq(originalLanguage.LANGUAGE_ID))
                .leftJoin(FILM_CATEGORY)
                .on(FILM.FILM_ID.eq(FILM_CATEGORY.FILM_ID))
                .leftJoin(CATEGORY)
                .on(FILM_CATEGORY.CATEGORY_ID.eq(CATEGORY.CATEGORY_ID))
                .leftJoin(FILM_ACTOR)
                .on(FILM.FILM_ID.eq(FILM_ACTOR.FILM_ID))
                .leftJoin(ACTOR)
                .on(FILM_ACTOR.ACTOR_ID.eq(ACTOR.ACTOR_ID))
                .where(whereCondition)
                .fetchStream()
                .map(Records.mapping(FilmRow::new))
                .distinct()
                .filter(Objects::nonNull)
                .map(FilmRow::toModel)
                .collect(ImmutableList.toImmutableList());
    }
}
