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
 * Film actor JPA entity. Each instance represents a row in the corresponding table.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "FilmActor")
public class FilmActorEntity {

    // See https://www.baeldung.com/jpa-many-to-many

    @EmbeddedId
    private FilmActorKey id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId(FilmActorKey_.FILM_ID)
    @JoinColumn(name = "film_id", referencedColumnName = "film_id")
    private FilmEntity film;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId(FilmActorKey_.ACTOR_ID)
    @JoinColumn(name = "actor_id", referencedColumnName = "actor_id")
    private ActorEntity actor;

    @Column(name = "last_update", nullable = false)
    private Instant lastUpdate;

    public FilmActorKey getId() {
        return id;
    }

    public void setId(FilmActorKey id) {
        this.id = id;
    }

    public FilmEntity getFilm() {
        return film;
    }

    public void setFilm(FilmEntity film) {
        this.film = film;
    }

    public ActorEntity getActor() {
        return actor;
    }

    public void setActor(ActorEntity actor) {
        this.actor = actor;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
