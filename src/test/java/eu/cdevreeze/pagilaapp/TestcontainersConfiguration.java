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

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Test containers configuration for the PostgreSQL Docker container.
 * <p>
 * See <a href="https://www.docker.com/blog/testcontainers-best-practices/">Testcontainers best practices</a>.
 *
 * @author Chris de Vreeze
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
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
    }

    @DynamicPropertySource
    void configureProperties(DynamicPropertyRegistry registry) {
        // Filling/overriding properties that in the running application come from application.properties
        registry.add("spring.datasource.url", () -> postgresContainer().getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgresContainer().getUsername());
        registry.add("spring.datasource.password", () -> postgresContainer().getPassword());
    }

}
