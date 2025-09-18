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
import eu.cdevreeze.pagilaapp.entity.conversions.EntityConversions;
import eu.cdevreeze.pagilaapp.model.Film;
import eu.cdevreeze.pagilaapp.service.FilmService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Subgraph;
import jakarta.persistence.criteria.*;
import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Default FilmService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
@ConditionalOnBooleanProperty(name = "useJooq", havingValue = false, matchIfMissing = true)
public class DefaultFilmService implements FilmService {

    // See https://thorben-janssen.com/hibernate-tips-how-to-bootstrap-hibernate-with-spring-boot/

    private static final String LOAD_GRAPH_KEY = "jakarta.persistence.loadgraph";

    // Shared thread-safe proxy for the actual transactional EntityManager that differs for each transaction
    @PersistenceContext
    private final EntityManager entityManager;

    private final int numberOfJpaQueriesToGetAllFilms;

    public DefaultFilmService(
            EntityManager entityManager,
            @Value("${numberOfJpaQueriesToGetAllFilms}") int numberOfJpaQueriesToGetAllFilms
    ) {
        this.entityManager = entityManager;
        this.numberOfJpaQueriesToGetAllFilms = numberOfJpaQueriesToGetAllFilms;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findAllFilms() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());
        System.out.println("Hibernate SessionImpl: " + entityManager.unwrap(SessionImpl.class));

        // Trying to find all films the same way as the queries below did not work. It led to loss of film actor data,
        // and lots of film data duplication. Hence, the use of multiple queries, combining the results afterward.

        Set<String> categories = findAllFilmCategories();

        int queryCount = numberOfJpaQueriesToGetAllFilms;
        Map<Integer, List<String>> categoryGroups = categories
                .stream()
                .collect(Collectors.groupingBy(cat -> cat.hashCode() % queryCount));

        Map<Integer, List<Film>> filmGroups = categoryGroups
                .entrySet()
                .stream()
                .map(kv -> Map.entry(kv.getKey(), findFilmsByCategories(ImmutableSet.copyOf(kv.getValue()))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return filmGroups.values()
                .stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingInt(v -> v.idOption().orElse(-1)))
                .distinct()
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
                .map(EntityConversions::convertFilmEntityToModel)
                .sorted(Comparator.comparingInt(v -> v.idOption().orElse(-1)))
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findFilmsByCategory(String category) {
        return findFilmsByCategories(ImmutableSet.of(category));
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Film> findFilmsByCategories(ImmutableSet<String> categories) {
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
                cb.upper(categoryJoin.get(CategoryEntity_.name)).in(
                        categories.stream().map(String::toUpperCase).collect(Collectors.toSet())
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
                .map(EntityConversions::convertFilmEntityToModel)
                .sorted(Comparator.comparingInt(v -> v.idOption().orElse(-1)))
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
                .map(EntityConversions::convertFilmEntityToModel)
                .sorted(Comparator.comparingInt(v -> v.idOption().orElse(-1)))
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableSet<String> findAllFilmCategories() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());
        System.out.println("Hibernate SessionImpl: " + entityManager.unwrap(SessionImpl.class));

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);

        Root<CategoryEntity> categoryRoot = cq.from(CategoryEntity.class);
        cq.select(categoryRoot.get(CategoryEntity_.name));

        return entityManager.createQuery(cq)
                .getResultStream()
                .collect(ImmutableSet.toImmutableSet());
    }

    private EntityGraph<FilmEntity> createEntityGraph() {
        EntityGraph<FilmEntity> filmGraph = entityManager.createEntityGraph(FilmEntity.class);
        filmGraph.addAttributeNode(FilmEntity_.language);
        filmGraph.addAttributeNode(FilmEntity_.originalLanguage);

        filmGraph.addAttributeNode(FilmEntity_.categories);
        Subgraph<FilmCategoryEntity> filmCategorySubgraph = filmGraph.addElementSubgraph(FilmEntity_.categories);
        filmCategorySubgraph.addAttributeNode(FilmCategoryEntity_.category);

        filmGraph.addAttributeNode(FilmEntity_.actors);
        Subgraph<FilmActorEntity> filmActorSubgraph = filmGraph.addElementSubgraph(FilmEntity_.actors);
        filmActorSubgraph.addAttributeNode(FilmActorEntity_.actor);
        return filmGraph;
    }
}
