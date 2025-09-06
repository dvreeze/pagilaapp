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

package eu.cdevreeze.pagilaapp.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Film category JPA entity. Each instance represents a row in the corresponding table.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "FilmCategory")
public class FilmCategoryEntity {

    // See https://www.baeldung.com/jpa-many-to-many

    @EmbeddedId
    private FilmCategoryKey id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId(FilmCategoryKey_.FILM_ID)
    @JoinColumn(name = "film_id", referencedColumnName = "film_id")
    private FilmEntity film;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId(FilmCategoryKey_.CATEGORY_ID)
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private CategoryEntity category;

    @Column(name = "last_update", nullable = false)
    private Instant lastUpdate;

    public FilmCategoryKey getId() {
        return id;
    }

    public void setId(FilmCategoryKey id) {
        this.id = id;
    }

    public FilmEntity getFilm() {
        return film;
    }

    public void setFilm(FilmEntity film) {
        this.film = film;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
