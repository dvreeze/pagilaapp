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
import com.google.common.primitives.ImmutableIntArray;
import eu.cdevreeze.pagilaapp.model.Staff;
import eu.cdevreeze.pagilaapp.service.StaffService;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static eu.cdevreeze.pagilaapp.jooq.Tables.STAFF;
import static eu.cdevreeze.pagilaapp.jooq.Tables.STORE;
import static eu.cdevreeze.pagilaapp.jooq.tables.Address.ADDRESS;
import static eu.cdevreeze.pagilaapp.jooq.tables.City.CITY;
import static eu.cdevreeze.pagilaapp.jooq.tables.Country.COUNTRY;
import static org.jooq.impl.DSL.row;

/**
 * jOOQ StaffService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
@ConditionalOnBooleanProperty(name = "useJooq")
public class JooqStaffService implements StaffService {

    public record StaffRow(
            @Nullable Integer id,
            String firstName,
            String lastName,
            ResultRows.AddressRow address,
            @Nullable String email,
            ResultRows.StoreRow store,
            boolean isActive,
            String userName,
            @Nullable String password,
            byte @Nullable [] picture
    ) {

        public Staff toModel() {
            Optional<byte[]> pictureByteArrayOption = Optional.ofNullable(picture);
            Optional<ImmutableIntArray> pictureOption = pictureByteArrayOption.map(pic ->
                    ImmutableIntArray
                            .builder()
                            .addAll(IntStream.range(0, pic.length).map(idx -> pic[idx]))
                            .build()
            );

            return new Staff(
                    Optional.ofNullable(id).stream().mapToInt(i -> i).findFirst(),
                    firstName,
                    lastName,
                    address.toModel(),
                    Optional.ofNullable(email),
                    store.toModel(),
                    isActive,
                    userName,
                    Optional.ofNullable(password),
                    pictureOption
            );
        }
    }

    private final DSLContext dsl;

    public JooqStaffService(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Staff> findAllStaffMembers() {
        var storeAddress = ADDRESS.as("STORE_ADDRESS");
        var storeCity = CITY.as("STORE_CITY");
        var storeCountry = COUNTRY.as("STORE_COUNTRY");

        return dsl
                .selectDistinct(
                        STAFF.STAFF_ID,
                        STAFF.FIRST_NAME,
                        STAFF.LAST_NAME,
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
                        STAFF.EMAIL,
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
                        STAFF.ACTIVE,
                        STAFF.USERNAME,
                        STAFF.PASSWORD,
                        STAFF.PICTURE
                )
                .from(STAFF)
                .leftJoin(STORE)
                .on(STAFF.STORE_ID.eq(STORE.STORE_ID))
                .leftJoin(storeAddress)
                .on(STORE.ADDRESS_ID.eq(storeAddress.ADDRESS_ID))
                .leftJoin(storeCity)
                .on(storeAddress.CITY_ID.eq(storeCity.CITY_ID))
                .leftJoin(storeCountry)
                .on(storeCity.COUNTRY_ID.eq(storeCountry.COUNTRY_ID))
                .leftJoin(ADDRESS)
                .on(STAFF.ADDRESS_ID.eq(ADDRESS.ADDRESS_ID))
                .leftJoin(CITY)
                .on(ADDRESS.CITY_ID.eq(CITY.CITY_ID))
                .leftJoin(COUNTRY)
                .on(CITY.COUNTRY_ID.eq(COUNTRY.COUNTRY_ID))
                .orderBy(STAFF.STAFF_ID)
                .fetchStream()
                .map(Records.mapping(StaffRow::new))
                .filter(Objects::nonNull)
                .map(StaffRow::toModel)
                .collect(ImmutableList.toImmutableList());
    }
}
