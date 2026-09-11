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

package eu.cdevreeze.pagilaapp.entity.conversions;

import module java.base;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.ImmutableIntArray;
import eu.cdevreeze.pagilaapp.entity.*;
import eu.cdevreeze.pagilaapp.model.*;

/**
 * Conversions from entities to model objects.
 * <p>
 * It is assumed that all associations that are needed to fill the returned model objects
 * have been loaded into the entities that are converted. This prevents "lazy loading exceptions"
 * when called outside a persistence context, and this prevents additional queries (and the dreaded
 * N + 1 problem) when called within a persistence context.
 *
 * @author Chris de Vreeze
 */
public class EntityConversions {

    private EntityConversions() {
    }

    public static Customer convertCustomerEntityToModel(CustomerEntity customerEntity) {
        return new Customer(
                customerEntity.getId(),
                convertStoreEntityToModel(customerEntity.getStore()),
                customerEntity.getFirstName(),
                customerEntity.getLastName(),
                customerEntity.getEmail(),
                convertAddressEntityToModel(customerEntity.getAddress()),
                Optional.ofNullable(customerEntity.getActive()).stream().anyMatch(v -> v == 1),
                customerEntity.getCreateDate()
        );
    }

    public static Staff convertStaffEntityToModel(StaffEntity staffEntity) {
        Optional<byte[]> pictureByteArrayOption = Optional.ofNullable(staffEntity.getPicture());
        Optional<ImmutableIntArray> pictureOption = pictureByteArrayOption.map(pic ->
                ImmutableIntArray
                        .builder()
                        .addAll(IntStream.range(0, pic.length).map(idx -> pic[idx]))
                        .build()
        );

        return new Staff(
                staffEntity.getId(),
                staffEntity.getFirstName(),
                staffEntity.getLastName(),
                convertAddressEntityToModel(staffEntity.getAddress()),
                staffEntity.getEmail(),
                convertStoreEntityToModel(staffEntity.getStore()),
                staffEntity.getActive(),
                staffEntity.getUserName(),
                staffEntity.getPassword(),
                pictureOption.orElse(null)
        );
    }

    public static Store convertStoreEntityToModel(StoreEntity storeEntity) {
        return new Store(
                storeEntity.getId(),
                convertAddressEntityToModel(storeEntity.getAddress())
        );
    }

    public static Address convertAddressEntityToModel(AddressEntity addressEntity) {
        return new Address(
                addressEntity.getId(),
                addressEntity.getAddress(),
                addressEntity.getAddress2(),
                addressEntity.getDistrict(),
                convertCityEntityToModel(addressEntity.getCity()),
                addressEntity.getPostalCode(),
                addressEntity.getPhone()
        );
    }

    public static City convertCityEntityToModel(CityEntity cityEntity) {
        return new City(
                cityEntity.getId(),
                cityEntity.getCity(),
                cityEntity.getCountry().getCountry()
        );
    }

    public static Film convertFilmEntityToModel(FilmEntity filmEntity) {
        return new Film(
                filmEntity.getId(),
                filmEntity.getTitle(),
                filmEntity.getDescription(),
                filmEntity.getReleaseYear(),
                filmEntity.getLanguage().getRawName().strip(),
                Optional.ofNullable(filmEntity.getOriginalLanguage()).map(LanguageEntity::getRawName).map(String::strip).orElse(null),
                filmEntity.getCategories()
                        .stream()
                        .map(EntityConversions::convertCategoryEntityToModel)
                        .collect(ImmutableSet.toImmutableSet()),
                Optional.ofNullable(filmEntity.getActors()).orElse(Set.of())
                        .stream()
                        .map(EntityConversions::convertActorEntityToModel)
                        .collect(ImmutableSet.toImmutableSet()),
                filmEntity.getRentalDuration(),
                filmEntity.getRentalRate(),
                Stream.ofNullable(filmEntity.getLength()).map(Integer::valueOf).findFirst().orElse(null),
                filmEntity.getReplacementCost(),
                filmEntity.getRating(),
                Optional.ofNullable(filmEntity.getSpecialFeatures())
                        .map(specFeatures ->
                                specFeatures.stream().collect(ImmutableSet.toImmutableSet())
                        )
                        .orElse(null)
        );
    }

    public static Actor convertActorEntityToModel(ActorEntity actorEntity) {
        return new Actor(
                actorEntity.getId(),
                actorEntity.getFirstName(),
                actorEntity.getLastName()
        );
    }

    public static Category convertCategoryEntityToModel(CategoryEntity categoryEntity) {
        return new Category(
                categoryEntity.getId(),
                categoryEntity.getName()
        );
    }
}
