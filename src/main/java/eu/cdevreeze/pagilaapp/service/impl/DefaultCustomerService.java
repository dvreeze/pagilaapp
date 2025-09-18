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
import eu.cdevreeze.pagilaapp.entity.*;
import eu.cdevreeze.pagilaapp.entity.conversions.EntityConversions;
import eu.cdevreeze.pagilaapp.model.Customer;
import eu.cdevreeze.pagilaapp.service.CustomerService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Subgraph;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.internal.SessionImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Default CustomerService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
@ConditionalOnBooleanProperty(name = "useJooq", havingValue = false, matchIfMissing = true)
public class DefaultCustomerService implements CustomerService {

    // See https://thorben-janssen.com/hibernate-tips-how-to-bootstrap-hibernate-with-spring-boot/

    private static final String LOAD_GRAPH_KEY = "jakarta.persistence.loadgraph";

    // Shared thread-safe proxy for the actual transactional EntityManager that differs for each transaction
    @PersistenceContext
    private final EntityManager entityManager;

    public DefaultCustomerService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Customer> findAllCustomers() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());
        System.out.println("Hibernate SessionImpl: " + entityManager.unwrap(SessionImpl.class));

        // First build up the query (without worrying about the load/fetch graph)
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CustomerEntity> cq = cb.createQuery(CustomerEntity.class);

        Root<CustomerEntity> customerRoot = cq.from(CustomerEntity.class);
        cq.select(customerRoot);

        // Next build up the entity graph, to specify which associated data should be fetched
        // At the same time, this helps achieve good performance, by solving the N + 1 problem
        EntityGraph<CustomerEntity> customerGraph = createEntityGraph();

        // Run the query, providing the load graph as query hint
        // Note that JPA entities do not escape the persistence context
        return entityManager.createQuery(cq)
                .setHint(LOAD_GRAPH_KEY, customerGraph)
                .getResultStream()
                .map(EntityConversions::convertCustomerEntityToModel)
                .collect(ImmutableList.toImmutableList());
    }

    private EntityGraph<CustomerEntity> createEntityGraph() {
        EntityGraph<CustomerEntity> customerGraph = entityManager.createEntityGraph(CustomerEntity.class);

        // A bit much, maybe. Yes, I miss output data when calling method findAllCustomers.

        customerGraph.addAttributeNode(CustomerEntity_.store);
        Subgraph<StoreEntity> storeSubgraph = customerGraph.addSubgraph(CustomerEntity_.store);

        storeSubgraph.addAttributeNode(StoreEntity_.address);
        Subgraph<AddressEntity> storeAddressSubgraph = storeSubgraph.addSubgraph(StoreEntity_.address);

        storeAddressSubgraph.addAttributeNode(AddressEntity_.city);
        Subgraph<CityEntity> storeCitySubgraph = storeAddressSubgraph.addSubgraph(AddressEntity_.city);

        storeCitySubgraph.addAttributeNode(CityEntity_.country);

        customerGraph.addAttributeNode(CustomerEntity_.address);
        Subgraph<AddressEntity> addressSubgraph = customerGraph.addSubgraph(CustomerEntity_.address);

        addressSubgraph.addAttributeNode(AddressEntity_.city);
        Subgraph<CityEntity> citySubgraph = addressSubgraph.addSubgraph(AddressEntity_.city);

        citySubgraph.addAttributeNode(CityEntity_.country);

        return customerGraph;
    }
}
