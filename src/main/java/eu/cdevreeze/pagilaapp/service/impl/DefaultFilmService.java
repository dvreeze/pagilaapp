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

package eu.cdevreeze.pagilaapp.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import eu.cdevreeze.pagilaapp.entity.*;
import eu.cdevreeze.pagilaapp.model.Actor;
import eu.cdevreeze.pagilaapp.model.Category;
import eu.cdevreeze.pagilaapp.model.Film;
import eu.cdevreeze.pagilaapp.service.FilmService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Subgraph;
import jakarta.persistence.criteria.*;
import org.hibernate.internal.SessionImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Default FilmService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
public class DefaultFilmService implements FilmService {

    // See https://thorben-janssen.com/hibernate-tips-how-to-bootstrap-hibernate-with-spring-boot/

    private static final String LOAD_GRAPH_KEY = "jakarta.persistence.loadgraph";

    // Shared thread-safe proxy for the actual transactional EntityManager that differs for each transaction
    @PersistenceContext
    private final EntityManager entityManager;

    public DefaultFilmService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findAllFilms() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());
        System.out.println("Hibernate SessionImpl: " + entityManager.unwrap(SessionImpl.class));

        // Trying to find all films the same way as the queries below, but without using joins/where-clause, this caused
        // incomplete data to be returned. That is, the fetch joins with all the actors of the film did not take place.
        // Hence, we build up the result with a small (fixed) number of similar queries (much like the ones in the
        // other methods below), combining the results afterward.

        List<String> languages = List.of("English", "German", "French", "Italian", "Japanese", "Mandarin");

        List<BiFunction<CriteriaBuilder, Path<String>, Predicate>> langPredBuilders = languages.stream()
                .map(lang -> (BiFunction<CriteriaBuilder, Path<String>, Predicate>) (cb, langPathExpr) ->
                        cb.equal(
                                cb.upper(langPathExpr),
                                lang.toUpperCase().strip()
                        )).toList();
        BiFunction<CriteriaBuilder, Path<String>, Predicate> otherLangPredBuilder = (cb, langPathExpr) ->
                cb.not(
                        cb.or(langPredBuilders.stream().map(lpb -> lpb.apply(cb, langPathExpr)).toList())
                );
        List<BiFunction<CriteriaBuilder, Path<String>, Predicate>> allLangPredBuilders =
                Stream.of(langPredBuilders.stream(), Stream.of(otherLangPredBuilder))
                        .flatMap(b -> b)
                        .toList();

        return allLangPredBuilders.stream()
                .flatMap(b -> findFilmsByLanguage(b).stream())
                .sorted(Comparator.comparingInt(film -> film.idOption().orElse(-1)))
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findFilmsByLanguage(String language) {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());
        System.out.println("Hibernate SessionImpl: " + entityManager.unwrap(SessionImpl.class));

        // First build up the query (without worrying about the load/fetch graph)
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

        Root<FilmEntity> filmRoot = cq.from(FilmEntity.class);
        Join<FilmEntity, LanguageEntity> languageJoin = filmRoot.join(FilmEntity_.language);
        // No need to explicitly set a query parameter when using the Criteria API.
        // SQL injection is prevented, and the generated SQL is parameterized and the database can reuse the query plan for it.
        cq.where(
                cb.equal(
                        cb.upper(languageJoin.get(LanguageEntity_.rawName)), // cb.trim does not work here but apparently is not needed
                        language.toUpperCase().strip()
                )
        );
        cq.select(filmRoot);

        // Next build up the entity graph, to specify which associated data should be fetched
        // At the same time, this helps achieve good performance, by solving the N + 1 problem
        EntityGraph<FilmEntity> filmGraph = createEntityGraph();

        // Run the query, providing the load graph as query hint
        // Note that JPA entities do not escape the persistence context
        return entityManager.createQuery(cq)
                .setHint(LOAD_GRAPH_KEY, filmGraph)
                .getResultStream()
                .map(this::convertEntityToModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findFilmsByCategory(String category) {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());
        System.out.println("Hibernate SessionImpl: " + entityManager.unwrap(SessionImpl.class));

        // First build up the query (without worrying about the load/fetch graph)
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

        Root<FilmEntity> filmRoot = cq.from(FilmEntity.class);
        Join<FilmEntity, FilmCategoryEntity> filmCategoryJoin = filmRoot.join(FilmEntity_.categories, JoinType.LEFT);
        Join<FilmCategoryEntity, CategoryEntity> categoryJoin = filmCategoryJoin.join(FilmCategoryEntity_.category, JoinType.LEFT);
        // No need to explicitly set a query parameter when using the Criteria API.
        // SQL injection is prevented, and the generated SQL is parameterized and the database can reuse the query plan for it.
        cq.where(
                cb.equal(
                        cb.upper(categoryJoin.get(CategoryEntity_.name)),
                        category.toUpperCase()
                )
        );
        cq.select(filmRoot);

        // Next build up the entity graph, to specify which associated data should be fetched
        // At the same time, this helps achieve good performance, by solving the N + 1 problem
        EntityGraph<FilmEntity> filmGraph = createEntityGraph();

        // Run the query, providing the load graph as query hint
        // Note that JPA entities do not escape the persistence context
        return entityManager.createQuery(cq)
                .setHint(LOAD_GRAPH_KEY, filmGraph)
                .getResultStream()
                .map(this::convertEntityToModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findFilmsByActor(String firstName, String lastName) {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());
        System.out.println("Hibernate SessionImpl: " + entityManager.unwrap(SessionImpl.class));

        // First build up the query (without worrying about the load/fetch graph)
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

        Root<FilmEntity> filmRoot = cq.from(FilmEntity.class);
        Join<FilmEntity, FilmActorEntity> filmActorJoin = filmRoot.join(FilmEntity_.actors, JoinType.LEFT);
        Join<FilmActorEntity, ActorEntity> actorJoin = filmActorJoin.join(FilmActorEntity_.actor, JoinType.LEFT);
        // No need to explicitly set a query parameter when using the Criteria API.
        // SQL injection is prevented, and the generated SQL is parameterized and the database can reuse the query plan for it.
        cq.where(
                cb.and(
                        cb.equal(
                                cb.upper(actorJoin.get(ActorEntity_.firstName)),
                                firstName.toUpperCase()
                        ),
                        cb.equal(
                                cb.upper(actorJoin.get(ActorEntity_.lastName)),
                                lastName.toUpperCase()
                        )
                )
        );
        cq.select(filmRoot);

        // Next build up the entity graph, to specify which associated data should be fetched
        // At the same time, this helps achieve good performance, by solving the N + 1 problem
        EntityGraph<FilmEntity> filmGraph = createEntityGraph();

        // Run the query, providing the load graph as query hint
        // Note that JPA entities do not escape the persistence context
        return entityManager.createQuery(cq)
                .setHint(LOAD_GRAPH_KEY, filmGraph)
                .getResultStream()
                .map(this::convertEntityToModel)
                .collect(ImmutableList.toImmutableList());
    }

    private ImmutableList<Film> findFilmsByLanguage(BiFunction<CriteriaBuilder, Path<String>, Predicate> languagePredicateBuilder) {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());

        // First build up the query (without worrying about the load/fetch graph)
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

        Root<FilmEntity> filmRoot = cq.from(FilmEntity.class);
        Join<FilmEntity, LanguageEntity> languageJoin = filmRoot.join(FilmEntity_.language);
        // No need to explicitly set a query parameter when using the Criteria API.
        // SQL injection is prevented, and the generated SQL is parameterized and the database can reuse the query plan for it.
        cq.where(
                languagePredicateBuilder.apply(cb, languageJoin.get(LanguageEntity_.rawName))
        );
        cq.select(filmRoot);

        // Next build up the entity graph, to specify which associated data should be fetched
        // At the same time, this helps achieve good performance, by solving the N + 1 problem
        EntityGraph<FilmEntity> filmGraph = createEntityGraph();

        // Run the query, providing the load graph as query hint
        // Note that JPA entities do not escape the persistence context
        return entityManager.createQuery(cq)
                .setHint(LOAD_GRAPH_KEY, filmGraph)
                .getResultStream()
                .map(this::convertEntityToModel)
                .collect(ImmutableList.toImmutableList());
    }

    private EntityGraph<FilmEntity> createEntityGraph() {
        EntityGraph<FilmEntity> filmGraph = entityManager.createEntityGraph(FilmEntity.class);
        filmGraph.addSubgraph(FilmEntity_.language);
        filmGraph.addSubgraph(FilmEntity_.originalLanguage);
        Subgraph<FilmCategoryEntity> filmCategoryGraph = filmGraph.addElementSubgraph(FilmEntity_.categories);
        filmCategoryGraph.addSubgraph(FilmCategoryEntity_.category);
        Subgraph<FilmActorEntity> filmActorGraph = filmGraph.addElementSubgraph(FilmEntity_.actors);
        filmActorGraph.addSubgraph(FilmActorEntity_.actor);
        return filmGraph;
    }

    private Film convertEntityToModel(FilmEntity filmEntity) {
        // Called within the persistence context
        // The needed associated data has already been loaded, so this will not trigger the N + 1 problem
        return new Film(
                Stream.ofNullable(filmEntity.getId()).mapToInt(i -> i).findFirst(),
                filmEntity.getTitle(),
                Optional.ofNullable(filmEntity.getDescription()),
                Optional.ofNullable(filmEntity.getReleaseYear()),
                filmEntity.getLanguage().getRawName().strip(),
                Optional.ofNullable(filmEntity.getOriginalLanguage()).map(LanguageEntity::getRawName).map(String::strip),
                filmEntity.getCategories()
                        .stream()
                        .map(filmCat ->
                                new Category(
                                        Stream.ofNullable(filmCat.getCategory().getId()).mapToInt(i -> i).findFirst(),
                                        filmCat.getCategory().getName()
                                )
                        )
                        .collect(ImmutableSet.toImmutableSet()),
                filmEntity.getActors()
                        .stream()
                        .map(filmActor ->
                                new Actor(
                                        Stream.ofNullable(filmActor.getActor().getId()).mapToInt(i -> i).findFirst(),
                                        filmActor.getActor().getFirstName(),
                                        filmActor.getActor().getLastName()
                                )
                        )
                        .collect(ImmutableSet.toImmutableSet()),
                filmEntity.getRentalDuration(),
                filmEntity.getRentalRate(),
                Stream.ofNullable(filmEntity.getLength()).mapToInt(i -> i).findFirst(),
                filmEntity.getReplacementCost(),
                Optional.ofNullable(filmEntity.getRating()),
                Optional.ofNullable(filmEntity.getSpecialFeatures())
                        .map(specFeatures ->
                                specFeatures.stream().collect(ImmutableSet.toImmutableSet())
                        )
        );
    }
}
