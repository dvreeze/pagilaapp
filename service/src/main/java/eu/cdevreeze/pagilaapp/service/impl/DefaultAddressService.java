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

import module jakarta.persistence;
import module java.base;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.pagilaapp.entity.AddressEntity;
import eu.cdevreeze.pagilaapp.entity.AddressEntity_;
import eu.cdevreeze.pagilaapp.entity.CityEntity_;
import eu.cdevreeze.pagilaapp.entity.conversions.EntityConversions;
import eu.cdevreeze.pagilaapp.model.Address;
import eu.cdevreeze.pagilaapp.service.api.AddressService;
import org.hibernate.internal.StatelessSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Default AddressService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
@ConditionalOnBooleanProperty(name = "useJooq", havingValue = false, matchIfMissing = true)
public class DefaultAddressService implements AddressService {

    // See https://thorben-janssen.com/hibernate-tips-how-to-bootstrap-hibernate-with-spring-boot/

    private static final String LOAD_GRAPH_KEY = "jakarta.persistence.loadgraph";

    private static final Logger logger = LoggerFactory.getLogger(DefaultAddressService.class);

    // Shared thread-safe proxy for the actual transactional EntityAgent that differs for each transaction
    @PersistenceAgent
    private final EntityAgent entityAgent;

    public DefaultAddressService(EntityAgent entityAgent) {
        this.entityAgent = entityAgent;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Address> findAllAddresses() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive());
        logger.debug("Hibernate StatelessSessionImpl: {}", entityAgent.unwrap(StatelessSessionImpl.class));

        // First build up the query (without worrying about the load/fetch graph)
        CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
        CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

        Root<AddressEntity> addressRoot = cq.from(AddressEntity.class);
        cq.select(addressRoot);

        // Next build up the entity graph, to specify which associated data should be fetched
        // At the same time, this helps achieve good performance, by solving the N + 1 problem
        EntityGraph<AddressEntity> addressGraph = entityAgent.createEntityGraph(AddressEntity.class);
        addressGraph.addSubgraph(AddressEntity_.city).addAttributeNode(CityEntity_.country);

        // Run the query, providing the load graph as query hint
        // Note that JPA entities do not escape the stateless session
        // It is not efficient to first retrieve entities and then convert them to DTOs, but it is practical
        // Note that method getResultStream was avoided; thus I appear to avoid some data loss in the query
        return entityAgent.createQuery(cq)
                .setHint(LOAD_GRAPH_KEY, addressGraph)
                .getResultList()
                .stream()
                .map(EntityConversions::convertAddressEntityToModel)
                .sorted(Comparator.comparingInt(v -> v.idOption().orElse(-1)))
                .collect(ImmutableList.toImmutableList());
    }
}
