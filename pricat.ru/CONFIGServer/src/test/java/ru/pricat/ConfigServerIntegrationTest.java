package ru.pricat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=jdbc",
                "spring.config.location=classpath:/application-test.yml"
        }
)
@SuppressWarnings("unchecked")
class ConfigServerIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    static {
        postgres.start();
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnApplicationConfigFromDatabase() {
        // Given: в БД есть данные (Flyway загрузил R__0001_Insert_default_configurations.sql)

        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/application/default/master",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Map<String, Object> body = response.getBody();
        assertThat(body).containsKey("propertySources");

        Assertions.assertNotNull(body);
        Map<String, Object> firstSource = (Map<String, Object>) ((java.util.List<?>) body.get("propertySources")).getFirst();
        Map<String, Object> source = (Map<String, Object>) firstSource.get("source");

        assertThat(source)
                .containsEntry("eureka.client.service-url.defaultZone", "http://localhost:8761/eureka/")
                .containsEntry("spring.cloud.config.uri", "http://localhost:8888");
    }

    @Test
    void shouldReturnOnlyGlobalApplicationConfigForUnknownApplication() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/some-unknown-service/default/master",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Assertions.assertNotNull(response.getBody());
        List<Map<String, Object>> propertySources = (List<Map<String, Object>>) response.getBody().get("propertySources");

        // Должен быть ровно один источник — глобальный application
        assertThat(propertySources)
                .hasSize(1)
                .extracting("name")
                .containsExactly("application-default");

        // И он должен содержать глобальные свойства
        Map<String, Object> source = (Map<String, Object>) propertySources.getFirst().get("source");
        assertThat(source)
                .containsKey("eureka.client.service-url.defaultZone")
                .containsKey("spring.cloud.config.uri");
    }
}
