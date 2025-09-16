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
import eu.cdevreeze.pagilaapp.model.Customer;
import eu.cdevreeze.pagilaapp.service.CustomerService;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static eu.cdevreeze.pagilaapp.jooq.Tables.CUSTOMER;
import static eu.cdevreeze.pagilaapp.jooq.Tables.STORE;
import static eu.cdevreeze.pagilaapp.jooq.tables.Address.ADDRESS;
import static eu.cdevreeze.pagilaapp.jooq.tables.City.CITY;
import static eu.cdevreeze.pagilaapp.jooq.tables.Country.COUNTRY;
import static org.jooq.impl.DSL.row;

/**
 * jOOQ CustomerService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
@ConditionalOnBooleanProperty(name = "useJooq")
public class JooqCustomerService implements CustomerService {

    public record CustomerRow(
            @Nullable Integer id,
            ResultRows.StoreRow store,
            String firstName,
            String lastName,
            @Nullable String email,
            ResultRows.AddressRow address,
            @Nullable Integer isActive,
            LocalDate createDate
    ) {

        public Customer toModel() {
            return new Customer(
                    Optional.ofNullable(id).stream().mapToInt(i -> i).findFirst(),
                    store.toModel(),
                    firstName,
                    lastName,
                    Optional.ofNullable(email),
                    address.toModel(),
                    Optional.ofNullable(isActive).stream().anyMatch(b -> b == 1),
                    createDate
            );
        }
    }

    private final DSLContext dsl;

    public JooqCustomerService(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Customer> findAllCustomers() {
        var storeAddress = ADDRESS.as("STORE_ADDRESS");
        var storeCity = CITY.as("STORE_CITY");
        var storeCountry = COUNTRY.as("STORE_COUNTRY");

        return dsl
                .selectDistinct(
                        CUSTOMER.CUSTOMER_ID,
                        row(
                                STORE.STORE_ID,
                                row(
                                        storeAddress.ADDRESS_ID,
                                        storeAddress.ADDRESS_,
                                        storeAddress.ADDRESS2,
                                        storeAddress.DISTRICT,
                                        row(
                                                storeAddress.CITY_ID,
                                                storeCity.CITY_,
                                                storeCountry.COUNTRY_
                                        ).convertFrom(Records.mapping(ResultRows.CityRow::new)),
                                        storeAddress.POSTAL_CODE,
                                        storeAddress.PHONE
                                ).convertFrom(Records.mapping(ResultRows.AddressRow::new))
                        ).convertFrom(Records.mapping(ResultRows.StoreRow::new)),
                        CUSTOMER.FIRST_NAME,
                        CUSTOMER.LAST_NAME,
                        CUSTOMER.EMAIL,
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
                        ).convertFrom(Records.mapping(ResultRows.AddressRow::new)),
                        CUSTOMER.ACTIVE,
                        CUSTOMER.CREATE_DATE
                )
                .from(CUSTOMER)
                .leftJoin(STORE)
                .on(CUSTOMER.STORE_ID.eq(STORE.STORE_ID))
                .leftJoin(storeAddress)
                .on(STORE.ADDRESS_ID.eq(storeAddress.ADDRESS_ID))
                .leftJoin(storeCity)
                .on(storeAddress.CITY_ID.eq(storeCity.CITY_ID))
                .leftJoin(storeCountry)
                .on(storeCity.COUNTRY_ID.eq(storeCountry.COUNTRY_ID))
                .leftJoin(ADDRESS)
                .on(CUSTOMER.ADDRESS_ID.eq(ADDRESS.ADDRESS_ID))
                .leftJoin(CITY)
                .on(ADDRESS.CITY_ID.eq(CITY.CITY_ID))
                .leftJoin(COUNTRY)
                .on(CITY.COUNTRY_ID.eq(COUNTRY.COUNTRY_ID))
                .orderBy(CUSTOMER.CUSTOMER_ID)
                .fetchStream()
                .map(Records.mapping(CustomerRow::new))
                .filter(Objects::nonNull)
                .map(CustomerRow::toModel)
                .collect(ImmutableList.toImmutableList());
    }
}
