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
import eu.cdevreeze.pagilaapp.model.Store;
import eu.cdevreeze.pagilaapp.service.StoreService;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static eu.cdevreeze.pagilaapp.jooq.Tables.STORE;
import static eu.cdevreeze.pagilaapp.jooq.tables.Address.ADDRESS;
import static eu.cdevreeze.pagilaapp.jooq.tables.City.CITY;
import static eu.cdevreeze.pagilaapp.jooq.tables.Country.COUNTRY;
import static org.jooq.impl.DSL.row;

/**
 * jOOQ StoreService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
@ConditionalOnBooleanProperty(name = "useJooq")
public class JooqStoreService implements StoreService {

    private final DSLContext dsl;

    public JooqStoreService(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Store> findAllStores() {
        return dsl
                .selectDistinct(
                        STORE.STORE_ID,
                        row(
                                ADDRESS.ADDRESS_ID,
                                ADDRESS.ADDRESS_,
                                ADDRESS.ADDRESS2,
                                ADDRESS.DISTRICT,
                                row(
                                        ADDRESS.CITY_ID,
                                        CITY.CITY_,
                                        COUNTRY.COUNTRY_
                                ).convertFrom(Records.mapping(ResultRows.CityRow::new)),
                                ADDRESS.POSTAL_CODE,
                                ADDRESS.PHONE
                        ).convertFrom(Records.mapping(ResultRows.AddressRow::new))
                )
                .from(STORE)
                .leftJoin(ADDRESS)
                .on(STORE.ADDRESS_ID.eq(ADDRESS.ADDRESS_ID))
                .leftJoin(CITY)
                .on(ADDRESS.CITY_ID.eq(CITY.CITY_ID))
                .leftJoin(COUNTRY)
                .on(CITY.COUNTRY_ID.eq(COUNTRY.COUNTRY_ID))
                .orderBy(STORE.STORE_ID)
                .fetchStream()
                .map(Records.mapping(ResultRows.StoreRow::new))
                .filter(Objects::nonNull)
                .map(ResultRows.StoreRow::toModel)
                .collect(ImmutableList.toImmutableList());
    }
}
