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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Set;

/**
 * Film JPA entity. Each instance represents a row in the corresponding table.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "Film")
public class FilmEntity {

    @Id
    @Column(name = "film_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Film_seq_gen")
    @SequenceGenerator(name = "Film_seq_gen", sequenceName = "film_film_id_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    // Satisfying validation of the JPA entities against the schema, through trial-and-error using the columnDefinition attribute
    @Column(name = "release_year", columnDefinition = "year")
    private Year releaseYear;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", referencedColumnName = "language_id")
    private LanguageEntity language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_language_id", referencedColumnName = "language_id")
    private LanguageEntity originalLanguage;

    @OneToMany(mappedBy = FilmCategoryEntity_.FILM)
    private Set<FilmCategoryEntity> categories;

    @OneToMany(mappedBy = FilmActorEntity_.FILM)
    private Set<FilmActorEntity> actors;

    @Column(name = "rental_duration", nullable = false)
    private Short rentalDuration;

    @Column(name = "rental_rate", nullable = false)
    private BigDecimal rentalRate;

    @Column(name = "length")
    private Short length;

    @Column(name = "replacement_cost", nullable = false)
    private BigDecimal replacementCost;

    @Column(name = "rating")
    private String rating;

    @Column(name = "last_update", nullable = false)
    private Instant lastUpdate;

    @Column(name = "special_features")
    private List<String> specialFeatures;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Year getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Year releaseYear) {
        this.releaseYear = releaseYear;
    }

    public LanguageEntity getLanguage() {
        return language;
    }

    public void setLanguage(LanguageEntity language) {
        this.language = language;
    }

    public LanguageEntity getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(LanguageEntity originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public Set<FilmCategoryEntity> getCategories() {
        return categories;
    }

    public void setCategories(Set<FilmCategoryEntity> categories) {
        this.categories = categories;
    }

    public Set<FilmActorEntity> getActors() {
        return actors;
    }

    public void setActors(Set<FilmActorEntity> actors) {
        this.actors = actors;
    }

    public Short getRentalDuration() {
        return rentalDuration;
    }

    public void setRentalDuration(Short rentalDuration) {
        this.rentalDuration = rentalDuration;
    }

    public BigDecimal getRentalRate() {
        return rentalRate;
    }

    public void setRentalRate(BigDecimal rentalRate) {
        this.rentalRate = rentalRate;
    }

    public Short getLength() {
        return length;
    }

    public void setLength(Short length) {
        this.length = length;
    }

    public BigDecimal getReplacementCost() {
        return replacementCost;
    }

    public void setReplacementCost(BigDecimal replacementCost) {
        this.replacementCost = replacementCost;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<String> getSpecialFeatures() {
        return specialFeatures;
    }

    public void setSpecialFeatures(List<String> specialFeatures) {
        this.specialFeatures = specialFeatures;
    }
}
