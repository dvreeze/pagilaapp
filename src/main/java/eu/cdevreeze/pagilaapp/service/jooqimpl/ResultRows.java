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

import eu.cdevreeze.pagilaapp.model.Address;
import eu.cdevreeze.pagilaapp.model.City;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Shared result set row classes.
 *
 * @author Chris de Vreeze
 */
public class ResultRows {

    private ResultRows() {
    }

    public record CityRow(
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

    public record AddressRow(
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
}
