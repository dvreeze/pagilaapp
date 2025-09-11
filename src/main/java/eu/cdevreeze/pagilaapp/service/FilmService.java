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

package eu.cdevreeze.pagilaapp.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import eu.cdevreeze.pagilaapp.model.Film;

/**
 * API contract of a service for querying and managing films.
 *
 * @author Chris de Vreeze
 */
public interface FilmService {

    ImmutableList<Film> findAllFilms();

    ImmutableList<Film> findFilmsByLanguage(String language);

    ImmutableList<Film> findFilmsByCategory(String category);

    ImmutableList<Film> findFilmsByCategories(ImmutableSet<String> categories);

    ImmutableList<Film> findFilmsByActor(String firstName, String lastName);

    ImmutableSet<String> findAllFilmCategories();
}
