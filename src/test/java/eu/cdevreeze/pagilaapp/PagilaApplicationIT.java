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

package eu.cdevreeze.pagilaapp;

import eu.cdevreeze.pagilaapp.model.Address;
import eu.cdevreeze.pagilaapp.service.AddressService;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying that the application context loads property, using a PostgreSQL
 * test Docker container.
 * <p>
 * Note that this test does not interfere with the real program or with the "test program".
 * After all, both web server port and PostgreSQL port are generated non-used port numbers.
 *
 * @author Chris de Vreeze
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@NullUnmarked
class PagilaApplicationIT {

    @Autowired
    private AddressService addressService;

    @ServiceConnection
    private final static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("pagilaTest")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("pagila-schema.sql"),
                            "/docker-entrypoint-initdb.d/01-schema.sql"
                    )
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("pagila-data.sql"),
                            "/docker-entrypoint-initdb.d/02-data.sql"
                    )
                    .withExposedPorts(5432);

    @BeforeAll
    protected static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    protected static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    protected static void configureProperties(DynamicPropertyRegistry registry) {
        // Filling/overriding properties that in the running application come from application.properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
        List<Address> addresses = addressService.findAllAddresses();

        int numberOfAddresses = addresses.size();
        assertThat(numberOfAddresses).isGreaterThan(100);
    }

}
