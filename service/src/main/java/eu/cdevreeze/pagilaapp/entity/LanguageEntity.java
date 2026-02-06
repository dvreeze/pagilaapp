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

/**
 * Language JPA entity. Each instance represents a row in the corresponding table.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "Language")
public class LanguageEntity {

    @Id
    @Column(name = "language_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Language_seq_gen")
    @SequenceGenerator(name = "Language_seq_gen", sequenceName = "language_language_id_seq", allocationSize = 1)
    private Integer id;

    // Satisfying validation of the JPA entities against the schema, through trial-and-error using the columnDefinition attribute
    // Note that this field is called rawName, because it may contain traling spaces to reach the declared character length
    @Column(name = "name", nullable = false, columnDefinition = "bpchar")
    private String rawName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
    }
}
