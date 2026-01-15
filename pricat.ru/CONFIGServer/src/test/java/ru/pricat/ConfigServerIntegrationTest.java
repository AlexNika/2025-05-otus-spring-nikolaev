package ru.pricat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=jdbc",
                "spring.config.location=classpath:/application-test.yml"
        }
)
@Testcontainers
@SuppressWarnings({"unchecked", "resource"})
class ConfigServerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("configdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnApplicationConfigFromDatabase() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/application/default/master",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> body = response.getBody();
        assertThat(body).containsKey("propertySources");

        List<?> propertySources = (List<?>) Objects.requireNonNull(body).get("propertySources");
        assertThat(propertySources).isNotEmpty();

        Map<String, Object> firstSource = (Map<String, Object>) propertySources.getFirst();
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
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<Map<String, Object>> propertySources = (List<Map<String, Object>>)
                Objects.requireNonNull(response.getBody()).get("propertySources");

        assertThat(propertySources)
                .hasSize(1)
                .extracting("name")
                .containsExactly("application-default");

        Map<String, Object> source = (Map<String, Object>) propertySources.getFirst().get("source");
        assertThat(source)
                .containsKey("eureka.client.service-url.defaultZone")
                .containsKey("spring.cloud.config.uri");
    }
}