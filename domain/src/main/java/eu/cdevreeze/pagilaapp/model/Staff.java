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

package eu.cdevreeze.pagilaapp.model;

import com.google.common.primitives.ImmutableIntArray;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Immutable staff record. The ID, if any, is the technical primary key.
 *
 * @author Chris de Vreeze
 */
public record Staff(
        @Nullable Integer id,
        String firstName,
        String lastName,
        Address address,
        @Nullable String email,
        Store store,
        boolean isActive,
        String userName,
        @Nullable String password,
        @Nullable ImmutableIntArray picture // immutable, but not memory-efficient compared to byte[]
) {

    public OptionalInt idOption() {
        return id == null ? OptionalInt.empty() : OptionalInt.of(id);
    }

    public Optional<String> emailOption() {
        return Optional.ofNullable(email);
    }

    public Optional<String> passwordOption() {
        return Optional.ofNullable(password);
    }

    public Optional<ImmutableIntArray> pictureOption() {
        return Optional.ofNullable(picture);
    }

    public Optional<byte[]> pictureAsOptionalByteArray() {
        return pictureOption().map(pic -> {
            byte[] result = new byte[pic.length()];
            IntStream.range(0, pic.length()).forEach(idx -> result[idx] = (byte) pic.get(idx));
            return result;
        });
    }
}
