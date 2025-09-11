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
import eu.cdevreeze.pagilaapp.entity.AddressEntity;
import eu.cdevreeze.pagilaapp.entity.AddressEntity_;
import eu.cdevreeze.pagilaapp.entity.CityEntity;
import eu.cdevreeze.pagilaapp.entity.CityEntity_;
import eu.cdevreeze.pagilaapp.model.Address;
import eu.cdevreeze.pagilaapp.model.City;
import eu.cdevreeze.pagilaapp.service.AddressService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Subgraph;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.internal.SessionImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Default AddressService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
@Primary
public class DefaultAddressService implements AddressService {

    // See https://thorben-janssen.com/hibernate-tips-how-to-bootstrap-hibernate-with-spring-boot/

    private static final String LOAD_GRAPH_KEY = "jakarta.persistence.loadgraph";

    // Shared thread-safe proxy for the actual transactional EntityManager that differs for each transaction
    @PersistenceContext
    private final EntityManager entityManager;

    public DefaultAddressService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Address> findAllAddresses() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());
        System.out.println("Hibernate SessionImpl: " + entityManager.unwrap(SessionImpl.class));

        // First build up the query (without worrying about the load/fetch graph)
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

        Root<AddressEntity> addressRoot = cq.from(AddressEntity.class);
        cq.select(addressRoot);

        // Next build up the entity graph, to specify which associated data should be fetched
        // At the same time, this helps achieve good performance, by solving the N + 1 problem
        EntityGraph<AddressEntity> addressGraph = entityManager.createEntityGraph(AddressEntity.class);
        addressGraph.addAttributeNode(AddressEntity_.city);
        Subgraph<CityEntity> citySubgraph = addressGraph.addSubgraph(AddressEntity_.city);
        citySubgraph.addAttributeNode(CityEntity_.country);

        // Run the query, providing the load graph as query hint
        // Note that JPA entities do not escape the persistence context
        return entityManager.createQuery(cq)
                .setHint(LOAD_GRAPH_KEY, addressGraph)
                .getResultStream()
                .map(this::convertEntityToModel)
                .collect(ImmutableList.toImmutableList());
    }

    private Address convertEntityToModel(AddressEntity addressEntity) {
        // Called within the persistence context
        // The needed associated data has already been loaded, so this will not trigger the N + 1 problem
        return new Address(
                Stream.ofNullable(addressEntity.getId()).mapToInt(i -> i).findFirst(),
                addressEntity.getAddress(),
                Optional.ofNullable(addressEntity.getAddress2()),
                addressEntity.getDistrict(),
                new City(
                        Stream.ofNullable(addressEntity.getCity().getId()).mapToInt(i -> i).findFirst(),
                        addressEntity.getCity().getCity(),
                        addressEntity.getCity().getCountry().getCountry()
                ),
                Optional.ofNullable(addressEntity.getPostalCode()),
                addressEntity.getPhone()
        );
    }
}
