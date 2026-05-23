package com.secureshop;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared Testcontainers setup for integration tests.
 *
 * <p>Import this class in any test that needs a running database:
 *
 * <pre>{@code
 * @Import(TestcontainersConfiguration.class)
 * }</pre>
 *
 * <p>{@link ServiceConnection} wires the container's JDBC URL, username, and password directly into
 * Spring's datasource configuration — no manual property overrides needed.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:18.4");
    }
}
