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
import eu.cdevreeze.pagilaapp.model.Address;
import eu.cdevreeze.pagilaapp.model.City;
import eu.cdevreeze.pagilaapp.service.AddressService;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static eu.cdevreeze.pagilaapp.jooq.tables.Address.ADDRESS;
import static eu.cdevreeze.pagilaapp.jooq.tables.City.CITY;
import static eu.cdevreeze.pagilaapp.jooq.tables.Country.COUNTRY;
import static org.jooq.impl.DSL.row;

/**
 * jOOQ AddressService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
@ConditionalOnBooleanProperty(name = "useJooq")
public class JooqAddressService implements AddressService {

    private record CityRow(
            @Nullable Integer id,
            String city,
            String country
    ) {

        public City toModel() {
            return new City(
                    Optional.ofNullable(id).stream().mapToInt(i -> i).findFirst(),
                    city,
                    country
            );
        }
    }

    private record AddressRow(
            @Nullable Integer id,
            String address,
            @Nullable String address2,
            String district,
            CityRow city,
            @Nullable String postalCode,
            String phone
    ) {

        public Address toModel() {
            return new Address(
                    Optional.ofNullable(id).stream().mapToInt(i -> i).findFirst(),
                    address,
                    Optional.ofNullable(address2),
                    district,
                    city.toModel(),
                    Optional.ofNullable(postalCode),
                    phone
            );
        }
    }

    private final DSLContext dsl;

    public JooqAddressService(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Address> findAllAddresses() {
        return dsl
                .selectDistinct(
                        ADDRESS.ADDRESS_ID,
                        ADDRESS.ADDRESS_,
                        ADDRESS.ADDRESS2,
                        ADDRESS.DISTRICT,
                        row(
                                ADDRESS.CITY_ID,
                                CITY.CITY_,
                                COUNTRY.COUNTRY_
                        ).convertFrom(Records.mapping(CityRow::new)),
                        ADDRESS.POSTAL_CODE,
                        ADDRESS.PHONE
                )
                .from(ADDRESS)
                .leftJoin(CITY)
                .on(ADDRESS.CITY_ID.eq(CITY.CITY_ID))
                .leftJoin(COUNTRY)
                .on(CITY.COUNTRY_ID.eq(COUNTRY.COUNTRY_ID))
                .fetchStream()
                .map(Records.mapping(AddressRow::new))
                .filter(Objects::nonNull)
                .map(AddressRow::toModel)
                .collect(ImmutableList.toImmutableList());
    }
}
